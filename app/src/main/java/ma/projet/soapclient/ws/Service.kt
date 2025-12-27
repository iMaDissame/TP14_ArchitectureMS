package ma.projet.soapclient.ws

import ma.projet.soapclient.beans.Compte
import ma.projet.soapclient.beans.TypeCompte
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*

class Service {
    private val NAMESPACE = "http://ws.demo.example.com/"
    private val URL = "http://10.0.2.2:8082/services/ws"
    private val METHOD_GET_COMPTES = "getComptes"
    private val METHOD_CREATE_COMPTE = "createCompte"
    private val METHOD_DELETE_COMPTE = "deleteCompte"

    fun getComptes(): List<Compte> {
        val request = SoapObject(NAMESPACE, METHOD_GET_COMPTES)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }
        val transport = HttpTransportSE(URL)
        val comptes = mutableListOf<Compte>()

        try {
            transport.call("", envelope)
            val response = envelope.bodyIn as SoapObject
            for (i in 0 until response.propertyCount) {
                val soapCompte = response.getProperty(i) as SoapObject
                val compte = Compte(
                    id = soapCompte.getPropertySafelyAsString("id")?.toLongOrNull(),
                    solde = soapCompte.getPropertySafelyAsString("solde")?.toDoubleOrNull() ?: 0.0,
                    dateCreation = SimpleDateFormat("yyyy-MM-dd").parse(
                        soapCompte.getPropertySafelyAsString("dateCreation")
                    ) ?: Date(),
                    type = TypeCompte.valueOf(soapCompte.getPropertySafelyAsString("type"))
                )
                comptes.add(compte)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return comptes
    }

    fun createCompte(solde: Double, type: TypeCompte): Boolean {
        val request = SoapObject(NAMESPACE, METHOD_CREATE_COMPTE).apply {
            addProperty("solde", solde.toString())
            addProperty("type", type.name)
        }
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }
        val transport = HttpTransportSE(URL)

        return try {
            transport.call("", envelope)
            val response = envelope.response
            when (response) {
                is org.ksoap2.serialization.SoapPrimitive -> response.toString().toBoolean()
                is Boolean -> response
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteCompte(id: Long): Boolean {
        val request = SoapObject(NAMESPACE, METHOD_DELETE_COMPTE).apply {
            addProperty("id", id)
        }
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }
        val transport = HttpTransportSE(URL)

        return try {
            transport.call("", envelope)
            val response = envelope.response
            when (response) {
                is org.ksoap2.serialization.SoapPrimitive -> response.toString().toBoolean()
                is Boolean -> response
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

fun SoapObject.getPropertySafelyAsString(name: String): String? {
    return try {
        val prop = this.getProperty(name)
        if (prop.toString() == "anyType{}") null else prop.toString()
    } catch (e: Exception) {
        null
    }
}