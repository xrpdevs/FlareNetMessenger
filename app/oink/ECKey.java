package uk.co.xrpdevs.flarenetmessenger;

import androidx.annotation.Nullable;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

//import android.support.annotation.Nullable;

import org.ethereum.config.Constants;
import org.ethereum.crypto.HashUtil;
import org.ethereum.crypto.jce.ECKeyAgreement;
import org.ethereum.crypto.jce.ECKeyFactory;
import org.ethereum.crypto.jce.ECKeyPairGenerator;
import org.ethereum.crypto.jce.ECSignatureFactory;
import org.ethereum.util.BIUtil;
import org.ethereum.util.ByteUtil;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.jce.spec.ECPublicKeySpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve.Fp;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
public class ECKey implements Serializable {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    public static final ECDomainParameters CURVE;
    public static final ECParameterSpec CURVE_SPEC;
    public static final BigInteger HALF_CURVE_ORDER;
    private static final SecureRandom secureRandom;
    private static final long serialVersionUID = -728224901792295832L;
    private final PrivateKey privKey;
    protected final ECPoint pub;
    private final Provider provider;
    private transient byte[] pubKeyHash;
    private transient byte[] nodeId;

    public ECKey() {
        this(secureRandom);
    }

    private static ECPoint extractPublicKey(ECPublicKey ecPublicKey) {
        java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
        BigInteger xCoord = publicPointW.getAffineX();
        BigInteger yCoord = publicPointW.getAffineY();
        return CURVE.getCurve().createPoint(xCoord, yCoord);
    }

    public ECKey(Provider provider, SecureRandom secureRandom) {
        this.provider = provider;
        KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privKey = keyPair.getPrivate();
        PublicKey pubKey = keyPair.getPublic();
        if(pubKey instanceof BCECPublicKey) {
            this.pub = ((BCECPublicKey)pubKey).getQ();
        } else {
            if(!(pubKey instanceof ECPublicKey)) {
                throw new AssertionError("Expected Provider " + provider.getName() + " to produce a subtype of ECPublicKey, found " + pubKey.getClass());
            }

            this.pub = extractPublicKey((ECPublicKey)pubKey);
        }

    }

    public ECKey(SecureRandom secureRandom) {
        this(new org.spongycastle.jce.provider.BouncyCastleProvider(), secureRandom);
    }

    private static boolean isECPrivateKey(PrivateKey privKey) {
        return privKey instanceof ECPrivateKey || privKey.getAlgorithm().equals("EC");
    }

    public ECKey(Provider provider,  PrivateKey privKey, ECPoint pub) {
        this.provider = provider;
        if(privKey != null && !isECPrivateKey(privKey)) {
            throw new IllegalArgumentException("Expected EC private key, given a private key object with class " + privKey.getClass().toString() + " and algorithm " + privKey.getAlgorithm());
        } else {
            this.privKey = privKey;
            if(pub == null) {
                throw new IllegalArgumentException("Public key may not be null");
            } else {
                this.pub = pub;
            }
        }
    }

    private static PrivateKey privateKeyFromBigInteger(BigInteger priv) {
        if(priv == null) {
            return null;
        } else {
            try {
                return ECKeyFactory.getInstance(new org.spongycastle.jce.provider.BouncyCastleProvider()).generatePrivate(new ECPrivateKeySpec(priv, CURVE_SPEC));
            } catch (InvalidKeySpecException var2) {
                throw new AssertionError("Assumed correct key spec statically");
            }
        }
    }

    public ECKey(@Nullable BigInteger priv, ECPoint pub) {
        this(new org.spongycastle.jce.provider.BouncyCastleProvider(), privateKeyFromBigInteger(priv), pub);
    }

    /** @deprecated */
    public static ECPoint compressPoint(ECPoint uncompressed) {
        return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
    }

    /** @deprecated */
    public static ECPoint decompressPoint(ECPoint compressed) {
        return CURVE.getCurve().decodePoint(compressed.getEncoded(false));
    }

    public static ECKey fromPrivate(BigInteger privKey) {
        return new ECKey(privKey, CURVE.getG().multiply(privKey));
    }

    public static ECKey fromPrivate(byte[] privKeyBytes) {
        return fromPrivate(new BigInteger(1, privKeyBytes));
    }

    public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
        return new ECKey(priv, pub);
    }

    public static ECKey fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
        check(priv != null, "Private key must not be null");
        check(pub != null, "Public key must not be null");
        return new ECKey(new BigInteger(1, priv), CURVE.getCurve().decodePoint(pub));
    }

    public static ECKey fromPublicOnly(ECPoint pub) {
        return new ECKey((BigInteger)null, pub);
    }

    public static ECKey fromPublicOnly(byte[] pub) {
        return new ECKey((BigInteger)null, CURVE.getCurve().decodePoint(pub));
    }

    /** @deprecated */
    public ECKey decompress() {
        return !this.pub.isCompressed()?this:new ECKey(this.provider, this.privKey, decompressPoint(this.pub));
    }

    /** @deprecated */
    public ECKey compress() {
        return this.pub.isCompressed()?this:new ECKey(this.provider, this.privKey, compressPoint(this.pub));
    }

    public boolean isPubKeyOnly() {
        return this.privKey == null;
    }

    public boolean hasPrivKey() {
        return this.privKey != null;
    }

    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = CURVE.getG().multiply(privKey);
        return point.getEncoded(compressed);
    }

    public static byte[] computeAddress(byte[] pubBytes) {
        return HashUtil.sha3omit12(Arrays.copyOfRange(pubBytes, 1, pubBytes.length));
    }

    public static byte[] computeAddress(ECPoint pubPoint) {
        return computeAddress(pubPoint.getEncoded(false));
    }

    public byte[] getAddress() {
        if(this.pubKeyHash == null) {
            this.pubKeyHash = computeAddress(this.pub);
        }

        return this.pubKeyHash;
    }

    public static byte[] pubBytesWithoutFormat(ECPoint pubPoint) {
        byte[] pubBytes = pubPoint.getEncoded(false);
        return Arrays.copyOfRange(pubBytes, 1, pubBytes.length);
    }

    public byte[] getNodeId() {
        if(this.nodeId == null) {
            this.nodeId = pubBytesWithoutFormat(this.pub);
        }

        return this.nodeId;
    }

    public static ECKey fromNodeId(byte[] nodeId) {
        check(nodeId.length == 64, "Expected a 64 byte node id");
        byte[] pubBytes = new byte[65];
        System.arraycopy(nodeId, 0, pubBytes, 1, nodeId.length);
        pubBytes[0] = 4;
        return fromPublicOnly(pubBytes);
    }

    public byte[] getPubKey() {
        return this.pub.getEncoded(false);
    }

    public ECPoint getPubKeyPoint() {
        return this.pub;
    }

    public BigInteger getPrivKey() {
        if(this.privKey == null) {
            throw new ECKey.MissingPrivateKeyException();
        } else if(this.privKey instanceof BCECPrivateKey) {
            return ((BCECPrivateKey)this.privKey).getD();
        } else {
            throw new ECKey.MissingPrivateKeyException();
        }
    }

    public boolean isCompressed() {
        return this.pub.isCompressed();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("pub:").append(Hex.toHexString(this.pub.getEncoded(false)));
        return b.toString();
    }

    public String toStringWithPrivate() {
        StringBuilder b = new StringBuilder();
        b.append(this.toString());
        if(this.privKey != null && this.privKey instanceof BCECPrivateKey) {
            b.append(" priv:").append(Hex.toHexString(((BCECPrivateKey)this.privKey).getD().toByteArray()));
        }

        return b.toString();
    }

    public ECKey.ECDSASignature doSign(byte[] input) {
        if(input.length != 32) {
            throw new IllegalArgumentException("Expected 32 byte input to ECDSA signature, not " + input.length);
        } else if(this.privKey == null) {
            throw new ECKey.MissingPrivateKeyException();
        } else if(this.privKey instanceof BCECPrivateKey) {
            ECDSASigner ex1 = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            ECPrivateKeyParameters derSignature1 = new ECPrivateKeyParameters(((BCECPrivateKey)this.privKey).getD(), CURVE);
            ex1.init(true, derSignature1);
            BigInteger[] components = ex1.generateSignature(input);
            return (new ECKey.ECDSASignature(components[0], components[1])).toCanonicalised();
        } else {
            try {
                Signature ex = ECSignatureFactory.getRawInstance(this.provider);
                ex.initSign(this.privKey);
                ex.update(input);
                byte[] derSignature = ex.sign();
                return ECKey.ECDSASignature.decodeFromDER(derSignature).toCanonicalised();
            } catch (InvalidKeyException | SignatureException var5) {
                throw new RuntimeException("ECKey signing error", var5);
            }
        }
    }

    public ECKey.ECDSASignature sign(byte[] messageHash) {
        ECKey.ECDSASignature sig = this.doSign(messageHash);
        int recId = -1;
        byte[] thisKey = this.pub.getEncoded(false);

        for(int i = 0; i < 4; ++i) {
            byte[] k = recoverPubBytesFromSignature(i, sig, messageHash);
            if(k != null && Arrays.equals(k, thisKey)) {
                recId = i;
                break;
            }
        }

        if(recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        } else {
            sig.v = (byte)(recId + 27);
            return sig;
        }
    }

    public static byte[] signatureToKeyBytes(byte[] messageHash, String signatureBase64) throws SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException var4) {
            throw new SignatureException("Could not decode base64", var4);
        }

        if(signatureEncoded.length < 65) {
            throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
        } else {
            return signatureToKeyBytes(messageHash, ECKey.ECDSASignature.fromComponents(Arrays.copyOfRange(signatureEncoded, 1, 33), Arrays.copyOfRange(signatureEncoded, 33, 65), (byte)(signatureEncoded[0] & 255)));
        }
    }

    public static byte[] signatureToKeyBytes(byte[] messageHash, ECKey.ECDSASignature sig) throws SignatureException {
        check(messageHash.length == 32, "messageHash argument has length " + messageHash.length);
        int header = sig.v;
        if(header >= 27 && header <= 34) {
            if(header >= 31) {
                header -= 4;
            }

            int recId = header - 27;
            byte[] key = recoverPubBytesFromSignature(recId, sig, messageHash);
            if(key == null) {
                throw new SignatureException("Could not recover public key from signature");
            } else {
                return key;
            }
        } else {
            throw new SignatureException("Header byte out of range: " + header);
        }
    }

    public static byte[] signatureToAddress(byte[] messageHash, String signatureBase64) throws SignatureException {
        return computeAddress(signatureToKeyBytes(messageHash, signatureBase64));
    }

    public static byte[] signatureToAddress(byte[] messageHash, ECKey.ECDSASignature sig) throws SignatureException {
        return computeAddress(signatureToKeyBytes(messageHash, sig));
    }

    public static ECKey signatureToKey(byte[] messageHash, String signatureBase64) throws SignatureException {
        byte[] keyBytes = signatureToKeyBytes(messageHash, signatureBase64);
        return fromPublicOnly(keyBytes);
    }

    public static ECKey signatureToKey(byte[] messageHash, ECKey.ECDSASignature sig) throws SignatureException {
        byte[] keyBytes = signatureToKeyBytes(messageHash, sig);
        return fromPublicOnly(keyBytes);
    }

    public BigInteger keyAgreement(ECPoint otherParty) {
        if(this.privKey == null) {
            throw new ECKey.MissingPrivateKeyException();
        } else if(this.privKey instanceof BCECPrivateKey) {
            ECDHBasicAgreement ex1 = new ECDHBasicAgreement();
            ex1.init(new ECPrivateKeyParameters(((BCECPrivateKey)this.privKey).getD(), CURVE));
            return ex1.calculateAgreement(new ECPublicKeyParameters(otherParty, CURVE));
        } else {
            try {
                KeyAgreement ex = ECKeyAgreement.getInstance(this.provider);
                ex.init(this.privKey);
                ex.doPhase(ECKeyFactory.getInstance(this.provider).generatePublic(new ECPublicKeySpec(otherParty, CURVE_SPEC)), true);
                return new BigInteger(1, ex.generateSecret());
            } catch (InvalidKeyException | InvalidKeySpecException | IllegalStateException var3) {
                throw new RuntimeException("ECDH key agreement failure", var3);
            }
        }
    }

    /** @deprecated */
    public byte[] decryptAES(byte[] cipher) {
        if(this.privKey == null) {
            throw new ECKey.MissingPrivateKeyException();
        } else if(!(this.privKey instanceof BCECPrivateKey)) {
            throw new UnsupportedOperationException("Cannot use the private key as an AES key");
        } else {
            AESFastEngine engine = new AESFastEngine();
            SICBlockCipher ctrEngine = new SICBlockCipher(engine);
            KeyParameter key = new KeyParameter(BigIntegers.asUnsignedByteArray(((BCECPrivateKey)this.privKey).getD()));
            ParametersWithIV params = new ParametersWithIV(key, new byte[16]);
            ctrEngine.init(false, params);
            int i = 0;
            byte[] out = new byte[cipher.length];

            while(i < cipher.length) {
                ctrEngine.processBlock(cipher, i, out, i);
                i += engine.getBlockSize();
                if(cipher.length - i < engine.getBlockSize()) {
                    break;
                }
            }

            if(cipher.length - i > 0) {
                byte[] tmpBlock = new byte[16];
                System.arraycopy(cipher, i, tmpBlock, 0, cipher.length - i);
                ctrEngine.processBlock(tmpBlock, 0, tmpBlock, 0);
                System.arraycopy(tmpBlock, 0, out, i, cipher.length - i);
            }

            return out;
        }
    }

    public static boolean verify(byte[] data, ECKey.ECDSASignature signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);

        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException var6) {
            return false;
        }
    }

    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        return verify(data, ECKey.ECDSASignature.decodeFromDER(signature), pub);
    }

    public boolean verify(byte[] data, byte[] signature) {
        return verify(data, signature, this.getPubKey());
    }

    public boolean verify(byte[] sigHash, ECKey.ECDSASignature signature) {
        return verify(sigHash, signature, this.getPubKey());
    }

    public boolean isPubKeyCanonical() {
        return isPubKeyCanonical(this.pub.getEncoded(false));
    }

    public static boolean isPubKeyCanonical(byte[] pubkey) {
        if(pubkey[0] == 4) {
            if(pubkey.length != 65) {
                return false;
            }
        } else {
            if(pubkey[0] != 2 && pubkey[0] != 3) {
                return false;
            }

            if(pubkey.length != 33) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    public static byte[] recoverPubBytesFromSignature(int recId, ECKey.ECDSASignature sig, byte[] messageHash) {
        check(recId >= 0, "recId must be positive");
        check(sig.r.signum() >= 0, "r must be positive");
        check(sig.s.signum() >= 0, "s must be positive");
        check(messageHash != null, "messageHash must not be null");
        BigInteger n = CURVE.getN();
        BigInteger i = BigInteger.valueOf((long)recId / 2L);
        BigInteger x = sig.r.add(i.multiply(n));
        Fp curve = (Fp)CURVE.getCurve();
        BigInteger prime = curve.getQ();
        if(x.compareTo(prime) >= 0) {
            return null;
        } else {
            ECPoint R = decompressKey(x, (recId & 1) == 1);
            if(!R.multiply(n).isInfinity()) {
                return null;
            } else {
                BigInteger e = new BigInteger(1, messageHash);
                BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
                BigInteger rInv = sig.r.modInverse(n);
                BigInteger srInv = rInv.multiply(sig.s).mod(n);
                BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
                org.spongycastle.math.ec.ECPoint.Fp q = (org.spongycastle.math.ec.ECPoint.Fp)ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
                return q.getEncoded(false);
            }
        }
    }

    @Nullable
    public static byte[] recoverAddressFromSignature(int recId, ECKey.ECDSASignature sig, byte[] messageHash) {
        byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
        return pubBytes == null?null:computeAddress(pubBytes);
    }

    @Nullable
    public static ECKey recoverFromSignature(int recId, ECKey.ECDSASignature sig, byte[] messageHash) {
        byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
        return pubBytes == null?null:fromPublicOnly(pubBytes);
    }

    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte)(yBit?3:2);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    @Nullable
    public byte[] getPrivKeyBytes() {
        return this.privKey == null?null:(this.privKey instanceof BCECPrivateKey?ByteUtil.bigIntegerToBytes(((BCECPrivateKey)this.privKey).getD(), 32):null);
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && o instanceof ECKey) {
            ECKey ecKey = (ECKey)o;
            return this.privKey != null && !this.privKey.equals(ecKey.privKey)?false:this.pub == null || this.pub.equals(ecKey.pub);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.getPubKey());
    }

    private static void check(boolean test, String message) {
        if(!test) {
            throw new IllegalArgumentException(message);
        }
    }

    static {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE_SPEC = new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());
        HALF_CURVE_ORDER = params.getN().shiftRight(1);
        secureRandom = new SecureRandom();
    }

    public static class MissingPrivateKeyException extends RuntimeException {
        public MissingPrivateKeyException() {
        }
    }

    public static class ECDSASignature {
        public final BigInteger r;
        public final BigInteger s;
        public byte v;

        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        private static ECKey.ECDSASignature fromComponents(byte[] r, byte[] s) {
            return new ECKey.ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        }

        public static ECKey.ECDSASignature fromComponents(byte[] r, byte[] s, byte v) {
            ECKey.ECDSASignature signature = fromComponents(r, s);
            signature.v = v;
            return signature;
        }

        public boolean validateComponents() {
            return validateComponents(this.r, this.s, this.v);
        }

        public static boolean validateComponents(BigInteger r, BigInteger s, byte v) {
            return v != 27 && v != 28?false:(BIUtil.isLessThan(r, BigInteger.ONE)?false:(BIUtil.isLessThan(s, BigInteger.ONE)?false:(!BIUtil.isLessThan(r, Constants.getSECP256K1N())?false:BIUtil.isLessThan(s, Constants.getSECP256K1N()))));
        }

        public static ECKey.ECDSASignature decodeFromDER(byte[] bytes) {
            ASN1InputStream decoder = null;

            ECKey.ECDSASignature e1;
            try {
                decoder = new ASN1InputStream(bytes);
                DLSequence e = (DLSequence)decoder.readObject();
                if(e == null) {
                    throw new RuntimeException("Reached past end of ASN.1 stream.");
                }

                ASN1Integer r;
                ASN1Integer s;
                try {
                    r = (ASN1Integer)e.getObjectAt(0);
                    s = (ASN1Integer)e.getObjectAt(1);
                } catch (ClassCastException var15) {
                    throw new IllegalArgumentException(var15);
                }

                e1 = new ECKey.ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException var16) {
                throw new RuntimeException(var16);
            } finally {
                if(decoder != null) {
                    try {
                        decoder.close();
                    } catch (IOException var14) {
                        ;
                    }
                }

            }

            return e1;
        }

        public ECKey.ECDSASignature toCanonicalised() {
            return this.s.compareTo(ECKey.HALF_CURVE_ORDER) > 0?new ECKey.ECDSASignature(this.r, ECKey.CURVE.getN().subtract(this.s)):this;
        }

        public String toBase64() {
            byte[] sigData = new byte[65];
            sigData[0] = this.v;
            System.arraycopy(ByteUtil.bigIntegerToBytes(this.r, 32), 0, sigData, 1, 32);
            System.arraycopy(ByteUtil.bigIntegerToBytes(this.s, 32), 0, sigData, 33, 32);
            return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
        }

        public boolean equals(Object o) {
            if(this == o) {
                return true;
            } else if(o != null && this.getClass() == o.getClass()) {
                ECKey.ECDSASignature signature = (ECKey.ECDSASignature)o;
                return !this.r.equals(signature.r)?false:this.s.equals(signature.s);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int result = this.r.hashCode();
            result = 31 * result + this.s.hashCode();
            return result;
        }
    }
}