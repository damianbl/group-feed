package com.dblazejewski.api.support

import akka.http.scaladsl.model.headers.HttpCredentials

import scala.concurrent.Future

final case class AuthenticatedUser(scheme: String, accessToken: String)

trait AuthorizationSupport {
  private val AuthorizationScheme = "AccessToken"
  private val AccessToken = "7ehrXcp6acX9"

  protected def tokenAuthenticator(credentials: Option[HttpCredentials]): Option[AuthenticatedUser] =
    credentials.map(c => AuthenticatedUser(c.scheme, c.token))

  protected def hasValidToken(user: Option[AuthenticatedUser]): Future[Boolean] =
    Future.successful(user.exists(u => u.scheme == AuthorizationScheme && u.accessToken == AccessToken))
}
