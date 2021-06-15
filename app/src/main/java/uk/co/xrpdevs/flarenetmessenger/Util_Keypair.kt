package uk.co.xrpdevs.flarenetmessenger

import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.web3j.crypto.ECKeyPair
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint

class Util_Keypair {

    private fun toEcPublicKey(publicKey: String): ECPublicKey {
        val params = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec = ECNamedCurveSpec("secp256k1", params.curve, params.g, params.n)

        //This is the part how to generate ECPoint manually from public key string.
        val pubKeyX = publicKey.substring(0, publicKey.length / 2)
        val pubKeyY = publicKey.substring(publicKey.length / 2)
        val ecPoint = ECPoint(BigInteger(pubKeyX, 16), BigInteger(pubKeyY, 16))

        val params2 = EC5Util.convertSpec(curveSpec.curve, params)

        val keySpec = java.security.spec.ECPublicKeySpec(ecPoint, params2)
        val factory = KeyFactory.getInstance("ECDSA")
        return factory.generatePublic(keySpec) as ECPublicKey
    }

    private fun toEcPrivateKey(privateKey: String): ECPrivateKey {
        val ecKeyPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey))

        val params = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec = ECNamedCurveSpec("secp256k1", params.curve, params.g, params.n)

        val keySpec = java.security.spec.ECPrivateKeySpec(
                ecKeyPair.privateKey,
                curveSpec)

        val factory = KeyFactory.getInstance("ECDSA")
        return factory.generatePrivate(keySpec) as ECPrivateKey
    }


}