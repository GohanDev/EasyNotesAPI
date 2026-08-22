package pt.ipt.easynotes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.application.install
import java.util.Date
private const val JWT_SECRET = "easynotes-secret-key"
private const val JWT_ISSUER = "easynotes-api"
private const val JWT_AUDIENCE = "easynotes-users"

fun Application.configureAuthentication() {

    install(Authentication) {

        jwt("auth-jwt") {

            realm = "EasyNotes"

            verifier(
                JWT
                    .require(
                        Algorithm.HMAC256(JWT_SECRET)
                    )
                    .withAudience(JWT_AUDIENCE)
                    .withIssuer(JWT_ISSUER)
                    .build()
            )

            validate { credential ->

                if (
                    credential.payload.audience.contains(JWT_AUDIENCE)
                ) {
                    JWTPrincipal(
                        credential.payload
                    )
                } else {
                    null
                }
            }
        }
    }
}

fun generateToken(userId: Int): String {

    return JWT.create()
        .withAudience(JWT_AUDIENCE)
        .withIssuer(JWT_ISSUER)
        .withClaim("userId", userId)
        .withExpiresAt(
            Date(System.currentTimeMillis() + 3_600_000) //1h em ms
        )
        .sign(
            Algorithm.HMAC256(JWT_SECRET)
        )
}