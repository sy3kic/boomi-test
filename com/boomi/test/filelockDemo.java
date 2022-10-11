// Copyright (c) 2022 Boomi, Inc.
package com.boomi.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.MessageFormat;

//java -cp filelockDemo.jar com.boomi.filelockDemo /<NFS directory path>/test.lock
public class filelockDemo {
	public static final int MAX_LOCKS = 10;
	public static final String FMT_FAILED = "Failed locking file {0}";
	public static final String FMT_LOCK = "Successfully locked file {0}";

	public static void main(String[] args) {
		FileChannel channel = null;
		FileLock fileLock = null;
		Boolean forUpdate = true;
		
		try {
			File lockFile = new File(args[0]);
			if (!lockFile.exists()) {
				lockFile.getParentFile().mkdirs();
				lockFile.createNewFile();
			}

			int tryLocks = 0;
			while (tryLocks < MAX_LOCKS) {
				try {
					// Attempt to acquire an exclusive lock
					channel = new RandomAccessFile(lockFile, "rw").getChannel();
					fileLock = channel.lock(0L, Long.MAX_VALUE, !forUpdate);
				} catch (IOException e) {
					e.printStackTrace();
				}				

				if (fileLock == null) {
					System.out.println(MessageFormat.format(FMT_FAILED, lockFile.getAbsolutePath()));
					Thread.sleep(10000);//10
				} else {
					System.out.println(MessageFormat.format(FMT_LOCK, lockFile.getAbsolutePath()));
					Thread.sleep(10000);//10
					releaseLockChannel(channel);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			releaseLockChannel(channel);
		}
	}

    private static void releaseLockChannel(FileChannel channel)
    {
        if(channel == null) {
			return;
		}
		
		try {
			channel.close();
			System.out.println("Unlocked file");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
}