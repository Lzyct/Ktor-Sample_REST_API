package com.tuxdev

import com.tuxdev.model.*
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.lang.Exception

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        port = 6969,
        watchPaths = listOf("."),
        module = Application::myModule
    ).start(wait = true)

}

fun Application.myModule() {
    install(StatusPages) {
        exception<Throwable> { e ->
            call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    val listStringGebetan = listOf("Ayu", "Sari", "Susan", "Dewi")
    val listObjectGebetan = mutableListOf<Gebetan>()
    for (i in listStringGebetan.indices) {
        listObjectGebetan.add(Gebetan(id = i + 1, nama = listStringGebetan[i]))
    }

    routing {
        get("/") {
            call.respondText("Hello Ktor World !!!")
        }

        route("/gebetan") {
            post {
                try {
                    val postParameters: Parameters = call.receiveParameters()
                    listObjectGebetan.add(
                        Gebetan(
                            listObjectGebetan[listObjectGebetan.lastIndex].id + 1,
                            postParameters["nama"].toString()
                        )
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        ResponseDiagnostic(Diagnostic(HttpStatusCode.OK.value, "${postParameters["nama"]} berhasil ditambahkan"))
                    )

                } catch (e: Exception) {
                    print(e)
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ResponseDiagnostic(Diagnostic(HttpStatusCode.BadRequest.value, "Nama tidak boleh kosong"))
                    )
                }

            }
            get {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = ResponseListSample(
                        diagnostic = Diagnostic(HttpStatusCode.OK.value, "Berhasil mendapatkan list nama gebetan"),
                        listGebetan = listObjectGebetan
                    )
                )
            }

            get("/detail") {

                val queryParameters: Parameters = call.request.queryParameters
                val detail = listObjectGebetan.find { it.id == queryParameters["id"]?.toInt() }
                if (detail == null)
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ResponseDiagnostic(Diagnostic(HttpStatusCode.BadRequest.value, "Gebetan tidak ditemukan T.T"))
                    )
                else call.respond(
                    status = HttpStatusCode.OK,
                    message = ResponseSample(
                        diagnostic = Diagnostic(HttpStatusCode.OK.value, "Berhasil mendapatkan nama gebetan"),
                        gebetan = detail
                    )
                )
            }
            put {
                try {
                    val putParameters: Parameters = call.receiveParameters()
                    val id = putParameters["id"]?.toInt()
                    val namaBaru: String? = putParameters["nama"]
                    if (namaBaru == null||id==null)
                        throw Exception()

                    val namaLama = listObjectGebetan.find { it.id == id }?.nama
                    listObjectGebetan.find { it.id == id }?.nama = namaBaru.toString()

                    call.respond(
                        HttpStatusCode.OK,
                       ResponseDiagnostic( Diagnostic(HttpStatusCode.OK.value, "$namaLama berhasil diperbarui menjadi $namaBaru"))
                    )
                } catch (e: Exception) {
                    print(e)
                    call.respond(
                        HttpStatusCode.BadRequest,
                       ResponseDiagnostic( Diagnostic(HttpStatusCode.BadRequest.value, "ID / Nama tidak boleh kosong"))
                    )
                }
            }
            delete{
                try {
                    val putParameters: Parameters = call.receiveParameters()
                    val id = putParameters["id"]?.toInt() ?: throw Exception()
                    val nama = listObjectGebetan.find { it.id == id }?.nama
                    val isRemove = listObjectGebetan.removeIf { it.id == id }

                    if(!isRemove)
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ResponseDiagnostic(Diagnostic(HttpStatusCode.BadRequest.value, "Gebetan tidak ditemukan T.T"))
                        )

                    call.respond(
                        HttpStatusCode.OK,
                        ResponseDiagnostic(Diagnostic(HttpStatusCode.OK.value, "$nama berhasil dihapus dari kenangan T.T"))
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ResponseDiagnostic(Diagnostic(HttpStatusCode.BadRequest.value, "ID tidak boleh kosong"))
                    )
                }
            }
        }

    }
}