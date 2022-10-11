// Copyright (c) 2022 Boomi, Inc.
package com.boomi.test;

import java.io.File;
import java.util.Random;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.MessageFormat;
import java.net.InetAddress;  

//java -cp filelockDemo.jar com.boomi.filelockDemo /<NFS directory path>/test.lock
public class filelockDemo {
	public static final int MAX_LOCKS = 100;
	public static final String FMT_WRITE = "{0} on {1} writing to file after lock\n";

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
				
				// randomize wait timeout
				int timeout = new Random().nextInt(1000);
				String fileContent = MessageFormat.format(FMT_WRITE, tryLocks, InetAddress.getLocalHost().getHostName());
				
				long startLock = System.currentTimeMillis();
				try {
					// Attempt to acquire an exclusive lock
					file = new RandomAccessFile(lockFile, "rw");
					channel = file.getChannel();
					fileLock = channel.lock(0L, Long.MAX_VALUE, !forUpdate);
				} catch (IOException e) {
					e.printStackTrace();
				}				

				if (fileLock == null) {
					System.out.println(MessageFormat.format( "Failed locking file {0}", lockFile.getAbsolutePath()));
					Thread.sleep(timeout);//1
				} else {
					System.out.println(MessageFormat.format("{0}: Locked file {1} in {2} ms", tryLocks, lockFile.getAbsolutePath(), System.currentTimeMillis()-startLock));
					if (file != null) {
						long writeTime = System.currentTimeMillis();
						file.writeChars(fileContent);
						System.out.println(MessageFormat.format("{0}: Write to file {1} in {2} ms", tryLocks, lockFile.getAbsolutePath(), System.currentTimeMillis()-writeTime));
					}
					long releaseLock = System.currentTimeMillis();
					releaseLockChannel(channel);
					System.out.println(MessageFormat.format("{0}: Unlocked file {1} in {2} ms", tryLocks, lockFile.getAbsolutePath(), System.currentTimeMillis()-releaseLock));
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