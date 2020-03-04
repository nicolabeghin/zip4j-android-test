package net.lingala.zip4j.tasks;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.headers.HeaderWriter;
import net.lingala.zip4j.io.outputstream.SplitOutputStream;
import net.lingala.zip4j.model.EndOfCentralDirectoryRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.enums.RandomAccessFileMode;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.tasks.RemoveEntryFromZipFileTask.RemoveEntryFromZipFileTaskParameters;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import static net.lingala.zip4j.headers.HeaderUtil.getIndexOfFileHeader;
import static net.lingala.zip4j.util.FileUtils.copyFile;

public class RemoveEntryFromZipFileTask extends AsyncZipTask<RemoveEntryFromZipFileTaskParameters>  {

  private ZipModel zipModel;

  public RemoveEntryFromZipFileTask(ZipModel zipModel, AsyncTaskParameters asyncTaskParameters) {
    super(asyncTaskParameters);
    this.zipModel = zipModel;
  }

  @Override
  protected void executeTask(RemoveEntryFromZipFileTaskParameters taskParameters, ProgressMonitor progressMonitor)
      throws IOException {
    if (zipModel.isSplitArchive()) {
      throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");
    }

    File temporaryZipFile = getTemporaryFile(zipModel.getZipFile().getPath());
    boolean successFlag = false;

    try (SplitOutputStream outputStream = new SplitOutputStream(temporaryZipFile);
         RandomAccessFile inputStream = new RandomAccessFile(zipModel.getZipFile(),
             RandomAccessFileMode.READ.getValue())){

      int indexOfFileHeader = getIndexOfFileHeader(zipModel, taskParameters.fileHeader);
      long offsetLocalFileHeader = getOffsetLocalFileHeader(taskParameters.fileHeader);
      long offsetStartOfCentralDirectory = getOffsetOfStartOfCentralDirectory(zipModel);
      List<FileHeader> fileHeaders = zipModel.getCentralDirectory().getFileHeaders();
      long offsetEndOfCompressedData = getOffsetEndOfCompressedData(indexOfFileHeader,
          offsetStartOfCentralDirectory, fileHeaders);

      if (indexOfFileHeader == 0) {
        if (zipModel.getCentralDirectory().getFileHeaders().size() > 1) {
          // if this is the only file and it is deleted then no need to do this
          copyFile(inputStream, outputStream, offsetEndOfCompressedData + 1,
              offsetStartOfCentralDirectory, progressMonitor);
        }
      } else if (indexOfFileHeader == fileHeaders.size() - 1) {
        copyFile(inputStream, outputStream, 0, offsetLocalFileHeader, progressMonitor);
      } else {
        copyFile(inputStream, outputStream, 0, offsetLocalFileHeader, progressMonitor);
        copyFile(inputStream, outputStream, offsetEndOfCompressedData + 1,
            offsetStartOfCentralDirectory, progressMonitor);
      }

      verifyIfTaskIsCancelled();

      updateHeaders(zipModel, outputStream, indexOfFileHeader, offsetEndOfCompressedData, offsetLocalFileHeader, taskParameters.charset);
      successFlag = true;
    } finally {
      cleanupFile(successFlag, zipModel.getZipFile(), temporaryZipFile);
    }
  }

  private long getOffsetOfStartOfCentralDirectory(ZipModel zipModel) {
    long offsetStartCentralDir = zipModel.getEndOfCentralDirectoryRecord().getOffsetOfStartOfCentralDirectory();

    if (zipModel.isZip64Format() && zipModel.getZip64EndOfCentralDirectoryRecord() != null) {
      offsetStartCentralDir = zipModel.getZip64EndOfCentralDirectoryRecord()
          .getOffsetStartCentralDirectoryWRTStartDiskNumber();
    }

    return offsetStartCentralDir;
  }

  private long getOffsetEndOfCompressedData(int indexOfFileHeader, long offsetStartOfCentralDirectory,
                                            List<FileHeader> fileHeaders) {
    if (indexOfFileHeader == fileHeaders.size() - 1) {
      return offsetStartOfCentralDirectory - 1;
    }

    FileHeader nextFileHeader = fileHeaders.get(indexOfFileHeader + 1);
    long offsetEndOfCompressedFile = nextFileHeader.getOffsetLocalHeader() - 1;
    if (nextFileHeader.getZip64ExtendedInfo() != null
        && nextFileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() != -1) {
      offsetEndOfCompressedFile = nextFileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() - 1;
    }

    return offsetEndOfCompressedFile;
  }

  private File getTemporaryFile(String zipPathWithName) {
    Random random = new Random();
    File tmpFile = new File(zipPathWithName + random.nextInt(10000));

    while (tmpFile.exists()) {
      tmpFile = new File(zipPathWithName + random.nextInt(10000));
    }

    return tmpFile;
  }

  private long getOffsetLocalFileHeader(FileHeader fileHeader) {
    long offsetLocalFileHeader = fileHeader.getOffsetLocalHeader();

    if (fileHeader.getZip64ExtendedInfo() != null && fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() != -1) {
      offsetLocalFileHeader = fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader();
    }

    return offsetLocalFileHeader;
  }

  private void updateHeaders(ZipModel zipModel, SplitOutputStream splitOutputStream, int indexOfFileHeader, long
      offsetEndOfCompressedFile, long offsetLocalFileHeader, Charset charset) throws IOException {

    updateEndOfCentralDirectoryRecord(zipModel, splitOutputStream);
    zipModel.getCentralDirectory().getFileHeaders().remove(indexOfFileHeader);
    updateFileHeadersWithLocalHeaderOffsets(zipModel.getCentralDirectory().getFileHeaders(), offsetEndOfCompressedFile,
        offsetLocalFileHeader);

    HeaderWriter headerWriter = new HeaderWriter();
    headerWriter.finalizeZipFile(zipModel, splitOutputStream, charset);
  }

  private void updateEndOfCentralDirectoryRecord(ZipModel zipModel, SplitOutputStream splitOutputStream)
      throws IOException {
    EndOfCentralDirectoryRecord endOfCentralDirectoryRecord = zipModel.getEndOfCentralDirectoryRecord();
    endOfCentralDirectoryRecord.setOffsetOfStartOfCentralDirectory(splitOutputStream.getFilePointer());
    endOfCentralDirectoryRecord.setTotalNumberOfEntriesInCentralDirectory(
        endOfCentralDirectoryRecord.getTotalNumberOfEntriesInCentralDirectory() - 1);
    endOfCentralDirectoryRecord.setTotalNumberOfEntriesInCentralDirectoryOnThisDisk(
        endOfCentralDirectoryRecord.getTotalNumberOfEntriesInCentralDirectoryOnThisDisk() - 1);
    zipModel.setEndOfCentralDirectoryRecord(endOfCentralDirectoryRecord);
  }

  private void updateFileHeadersWithLocalHeaderOffsets(List<FileHeader> fileHeaders, long offsetEndOfCompressedFile,
                                                       long offsetLocalFileHeader) {
    for (int i = 0; i < fileHeaders.size(); i ++) {
      FileHeader fileHeader = fileHeaders.get(i);
      long offsetLocalHdr = fileHeader.getOffsetLocalHeader();

      if (fileHeader.getZip64ExtendedInfo() != null && fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader() != -1) {
        offsetLocalHdr = fileHeader.getZip64ExtendedInfo().getOffsetLocalHeader();
      }

      if (offsetLocalHdr > offsetLocalFileHeader) {
        fileHeader.setOffsetLocalHeader(offsetLocalHdr - (offsetEndOfCompressedFile - offsetLocalFileHeader) - 1);
      }
    }
  }

  private void cleanupFile(boolean successFlag, File zipFile, File temporaryZipFile) throws ZipException {
    if (successFlag) {
      restoreFileName(zipFile, temporaryZipFile);
    } else {
      temporaryZipFile.delete();
    }
  }

  private void restoreFileName(File zipFile, File temporaryZipFile) throws ZipException {
    if (zipFile.delete()) {
      if (!temporaryZipFile.renameTo(zipFile)) {
        throw new ZipException("cannot rename modified zip file");
      }
    } else {
      throw new ZipException("cannot delete old zip file");
    }
  }

  @Override
  protected long calculateTotalWork(RemoveEntryFromZipFileTaskParameters taskParameters) {
    return zipModel.getZipFile().length() - taskParameters.fileHeader.getCompressedSize();
  }

  @Override
  protected ProgressMonitor.Task getTask() {
    return ProgressMonitor.Task.REMOVE_ENTRY;
  }

  public static class RemoveEntryFromZipFileTaskParameters extends AbstractZipTaskParameters {
    private FileHeader fileHeader;

    public RemoveEntryFromZipFileTaskParameters(FileHeader fileHeader, Charset charset) {
      super(charset);
      this.fileHeader = fileHeader;
    }
  }
}
