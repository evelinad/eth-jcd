package ch.se.inf.ethz.jcd.batman.io.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

/**
 * A {@link DataMover} implementation that encrypts imported files and
 * exports decrypted ones.
 * 
 * @see DataMover
 * @see Cipher
 *
 */
public class EncryptedMover implements DataMover {
    
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;
    
    public EncryptedMover(Cipher encrypt, Cipher decrypt) {
        this.encryptCipher = encrypt;
        this.decryptCipher = decrypt;
    }

    @Override
    public void importMove(InputStream hostSource, OutputStream virtualTarget)
            throws IOException {
        CipherOutputStream encryptedOut = new CipherOutputStream(virtualTarget, this.encryptCipher);
        
        DefaultMover.move(hostSource, encryptedOut);
        
        encryptedOut.close();
        hostSource.close();
    }

    @Override
    public void exportMove(InputStream virtualSource, OutputStream hostTarget)
            throws IOException {
        CipherInputStream decryptIn = new CipherInputStream(virtualSource, this.decryptCipher);
        
        DefaultMover.move(decryptIn, hostTarget);
        
        hostTarget.close();
        decryptIn.close();
    }

}
