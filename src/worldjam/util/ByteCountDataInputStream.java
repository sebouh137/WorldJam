package worldjam.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;


public class ByteCountDataInputStream extends DataInputStream implements ByteCounter{

	public ByteCountDataInputStream(InputStream in) {
		super(new WrapperInputStream(in));
		
	}


	public final static class WrapperInputStream extends InputStream {
		long pos;
		InputStream in;
		public WrapperInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public int read() throws IOException {
			pos +=1;
			return in.read();
		}

	    
	    public int read(byte b[]) throws IOException {
	        int ret = in.read(b);
	        pos += ret;
	        return ret;
	    }

	    
	    public int read(byte b[], int off, int len) throws IOException {
	       int ret = in.read(b,off,len);
	       pos += ret;
	       return ret;
	    }

	    
	    public long skip(long n) throws IOException {
	        long ret = in.skip(n);
	        pos += ret;
	        return ret;
	    }

	    public int available() throws IOException {
	        return in.available();
	    }

	    public void close() throws IOException {in.close();}

	    public synchronized void mark(int readlimit) {in.mark(readlimit);}

	    public synchronized void reset() throws IOException {
	    	in.reset();
	    }

	    public boolean markSupported() {
	        return in.markSupported();
	    }

	}

	@Override
	public long bytesProcessed() {
		return ((WrapperInputStream)in).pos;
	}

	
	
	

}
