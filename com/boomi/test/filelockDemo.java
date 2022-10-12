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
	public static final int MAX_LOCKS = 300;
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
				int timeout = new Random().nextInt(200);
				String fileContent = MessageFormat.format(FMT_WRITE, tryLocks, InetAddress.getLocalHost().getHostName());
				
				long startLock = System.nanoTime();
				System.out.println(MessageFormat.format( "{0}: Start lock try", tryLocks));
				try {
					// Attempt to acquire an exclusive lock
					long timeRandomAccessFile = System.nanoTime();
					System.out.println(MessageFormat.format( "{0}: Start RandomAccessFile", tryLocks));
					file = new RandomAccessFile(lockFile, "rw");
					System.out.println(MessageFormat.format( "{0}: Finish RandomAccessFile took {1}", tryLocks, System.nanoTime()-timeRandomAccessFile));
					long timeChannel = System.nanoTime();
					System.out.println(MessageFormat.format( "{0}: Start getChannel", tryLocks));
					channel = file.getChannel();
					System.out.println(MessageFormat.format( "{0}: Finish getChannel took {1}", tryLocks, System.nanoTime()-timeChannel));
					long timeLock = System.nanoTime();
					System.out.println(MessageFormat.format( "{0}: Start channel.lock", tryLocks));
					fileLock = channel.lock(0L, Long.MAX_VALUE, !forUpdate);
					System.out.println(MessageFormat.format( "{0}: Finish channel.lock took {1}", tryLocks, System.nanoTime()-timeLock));					
				} catch (IOException e) {
					e.printStackTrace();
				}				

				if (fileLock == null) {
					System.out.println(MessageFormat.format( "{0}: Failed locking file {1}", tryLocks, lockFile.getAbsolutePath()));
					Thread.sleep(timeout);//1
				} else {
					System.out.println(MessageFormat.format("{0}: Locked file {1} in {2} ns", tryLocks, lockFile.getAbsolutePath(), System.nanoTime()-startLock));
					if (file != null) {
						long writeTime = System.nanoTime();
						System.out.println(MessageFormat.format( "{0}: Start write to file", tryLocks));
						file.writeChars(fileContent);
						System.out.println(MessageFormat.format("{0}: Write to file {1} in {2} ns", tryLocks, lockFile.getAbsolutePath(), System.nanoTime()-writeTime));
					}
					long releaseLock = System.nanoTime();
					System.out.println(MessageFormat.format( "{0}: Start releaseLockChannel", tryLocks));
					releaseLockChannel(channel);
					System.out.println(MessageFormat.format("{0}: Unlocked file {1} in {2} ns", tryLocks, lockFile.getAbsolutePath(), System.nanoTime()-releaseLock));
					System.out.println(MessageFormat.format("{0}: Waiting for {1} ns", tryLocks, timeout));
					Thread.sleep(timeout);
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