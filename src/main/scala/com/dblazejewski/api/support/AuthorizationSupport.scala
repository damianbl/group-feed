package com.dblazejewski.api.support

import akka.http.scaladsl.model.headers.HttpCredentials
import com.dblazejewski.infrastructure.{Configuration, ConfigurationModuleImpl}

import scala.concurrent.Future

final case class AuthenticatedUser(scheme: String, accessToken: String)

trait AuthorizationSupport extends ConfigurationModuleImpl {
  this: Configuration =>

  private val AuthorizationScheme = config.getString("auth.scheme")
  private val AccessToken = config.getString("auth.accessToken")

  protected def tokenAuthenticator(credentials: Option[HttpCredentials]): Option[AuthenticatedUser] =
    credentials.map(c => AuthenticatedUser(c.scheme, c.token))

  protected def hasValidToken(user: Option[AuthenticatedUser]): Future[Boolean] =
    Future.successful(user.exists(u => u.scheme == AuthorizationScheme && u.accessToken == AccessToken))
}
