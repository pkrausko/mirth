package com.mirth.connect.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.Base64Util;

public class AttachmentUtil {
    public static void writeToFile(String filePath, Attachment attachment, boolean binary) throws IOException {
        File file = new File(filePath);
        if (!file.canWrite()) {
            String dirName = file.getPath();
            int i = dirName.lastIndexOf(File.separator);
            if (i > -1) {
                dirName = dirName.substring(0, i);
                File dir = new File(dirName);
                dir.mkdirs();
            }
            file.createNewFile();
        }

        if (attachment != null && StringUtils.isNotEmpty(filePath)) {
            FileUtils.writeByteArrayToFile(file, binary ? Base64Util.decodeBase64(attachment.getContent()) : attachment.getContent());
        }
    }
}