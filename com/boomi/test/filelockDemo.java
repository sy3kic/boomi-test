// Copyright (c) 2022 Boomi, Inc.
package com.boomi.test;

import java.io.File;
import java.util.Random;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.MessageFormat;

//java -cp filelockDemo.jar com.boomi.filelockDemo /<NFS directory path>/test.lock
public class filelockDemo {
	public static final int MAX_LOCKS = 100;
	public static final String FMT_FAILED = "Failed locking file {0}";
	public static final String FMT_LOCK = "{0}: Locked file {1}";
	public static final String FMT_UNLOCK = "{0}: Unlocked file {1}";
	public static final String FMT_WRITE = "{0}: Written to file {1}";

	public static void main(String[] args) {
		RandomAccessFile file = null;
		FileChannel channel = null;
		FileLock fileLock = null;
		Boolean forUpdate = true;
		
		try {
			File lockFile = new File(args[0]);
			if (!lockFile.exists()) {
				lockFile.getParentFile().mkdirs();
				lockFile.createNewFile();
			}

			int tryLocks = 1;
			while (tryLocks <= MAX_LOCKS) {
				try {
					// Attempt to acquire an exclusive lock
					file = new RandomAccessFile(lockFile, "rw");
					channel = file.getChannel();
					fileLock = channel.lock(0L, Long.MAX_VALUE, !forUpdate);
				} catch (IOException e) {
					e.printStackTrace();
				}				

				int timeout = new Random().nextInt(1000);

				if (fileLock == null) {
					System.out.println(MessageFormat.format(FMT_FAILED, lockFile.getAbsolutePath()));
					Thread.sleep(timeout);//1
				} else {
					System.out.println(MessageFormat.format(FMT_LOCK, tryLocks, lockFile.getAbsolutePath()));
					if (file != null) {
						file.writeChars("writing after lock");
						System.out.println(MessageFormat.format(FMT_WRITE, tryLocks, lockFile.getAbsolutePath()));
					}
					releaseLockChannel(channel);
					System.out.println(MessageFormat.format(FMT_UNLOCK, tryLocks, lockFile.getAbsolutePath()));
					Thread.sleep(timeout);//1
				}
				tryLocks++;
				System.out.println("-------");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
}