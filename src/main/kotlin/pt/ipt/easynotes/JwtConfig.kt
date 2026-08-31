package pt.ipt.easynotes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.util.Date

private const val JWT_ISSUER = "easynotes-api"
private const val JWT_AUDIENCE = "easynotes-users"

/*
 * Obtém o segredo JWT através de uma variável de ambiente.
 */
private val jwtSecret: String =
    System.getenv("JWT_SECRET")
        ?: "easynotes-local-development-secret"

fun Application.configureAuthentication() {

    install(Authentication) {

        jwt("auth-jwt") {

            realm = "EasyNotes"

            verifier(
                JWT
                    .require(
                        Algorithm.HMAC256(jwtSecret)
                    )
                    .withAudience(JWT_AUDIENCE)
                    .withIssuer(JWT_ISSUER)
                    .build()
            )

            validate { credential ->

                if (
                    credential.payload.audience.contains(
                        JWT_AUDIENCE
                    )
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

fun generateToken(
    userId: Int
): String {

    return JWT.create()
        .withAudience(JWT_AUDIENCE)
        .withIssuer(JWT_ISSUER)
        .withClaim(
            "userId",
            userId
        )
        .withExpiresAt(
            Date(
                System.currentTimeMillis()
                        + 3_600_000
            )
        )
        .sign(
            Algorithm.HMAC256(
                jwtSecret
            )
        )
}