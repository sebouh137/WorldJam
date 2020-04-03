package worldjam.util;

import java.io.DataOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;


public class ByteCountDataOutputStream extends DataOutputStream implements ByteCounter{

	public ByteCountDataOutputStream(OutputStream out) {
		super(new WrapperOutputStream(out));
		
	}


	public final static class WrapperOutputStream extends OutputStream {
		long pos;
		OutputStream out;
		public WrapperOutputStream(OutputStream out) {
			this.out = out;
		}
		@Override
		public void write(int b) throws IOException {
			out.write(b);
			pos ++;
		}

		public void write(byte b[]) throws IOException {
	        write(b, 0, b.length);
	        pos+= b.length;
	    }

	    
	    public void write(byte b[], int off, int len) throws IOException {
	        out.write(b,off,len);
	        pos+=len;
	    }

	    
	    public void flush() throws IOException {
	    	out.flush();
	    }

	    public void close() throws IOException {
	    	out.close();
	    }

	}

	@Override
	public long bytesProcessed() {
		return ((WrapperOutputStream)out).pos;
	}

	
	
	

}
