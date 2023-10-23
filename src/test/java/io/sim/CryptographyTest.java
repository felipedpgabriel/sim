package io.sim;

import static org.junit.Assert.*;
import org.junit.Test;
import io.sim.messages.Cryptography;

public class CryptographyTest {

    @Test
    public void testEncryptionAndDecryption() {
        try {
            String textoOriginal = "Texto original para teste";
            byte[] textoEncriptado = Cryptography.encrypt(textoOriginal);
            String textoDescriptado = Cryptography.decrypt(textoEncriptado);

            // Verificar se o texto descriptografado é igual ao texto original
            assertEquals(textoOriginal, textoDescriptado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
