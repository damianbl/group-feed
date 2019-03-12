package com.dblazejewski.support

import com.dblazejewski.support.ScalazSupport.{
  ErrorMessage,
  LiftedFutureResult,
  TaskResult,
  WrappedExceptionErrorMessage
}
import scalaz.{ EitherT, OptionT }
import scalaz.OptionT.optionT
import scalaz.Scalaz._

import scala.concurrent.{ ExecutionContext, Future }

trait ScalazSupport {

  protected def rightIf(futB: Future[Boolean], msg: String)(implicit ec: ExecutionContext): TaskResult[Boolean] =
    EitherT.fromEither(futB map { v =>
      if (v) Right(true)
      else Left(ErrorMessage(msg))
    } recover {
      case e: Throwable => Left(WrappedExceptionErrorMessage(e, s"exception in rightIf, $msg"))
    })

  protected def rightT[V](futV: Future[V])(implicit ec: ExecutionContext): TaskResult[V] = EitherT.fromEither {
    futV.map(v => Right(v)).recover {
      case e: Throwable => Left(WrappedExceptionErrorMessage(e, s"exception '${e.getMessage}'"))
    }
  }

  protected def rightT[V](futOptV: Future[Option[V]], msg: String)(implicit ec: ExecutionContext): TaskResult[V] =
    EitherT.fromEither(futOptV map {
      case None => Left(ErrorMessage(msg))
      case Some(v) => Right(v)
    } recover {
      case e: Throwable => Left(WrappedExceptionErrorMessage(e, s"exception in rightIf, $msg"))
    })

  protected def optT[A](a: Future[A])(implicit ec: ExecutionContext): LiftedFutureResult[A] = optionT(a.map(Option(_)))

  protected def optT[A](a: Future[Option[A]])(implicit ec: ExecutionContext, dummy: DummyImplicit): LiftedFutureResult[A] = optionT(a)

}

object ScalazSupport {

  type TaskResult[V] = EitherT[Future, Error, V]

  type LiftedFutureResult[A] = OptionT[Future, A]

  trait Error {
    val msg: String
  }

  case class ErrorMessage(msg: String) extends Error

  case class WrappedExceptionErrorMessage(exception: Throwable, msg: String) extends Error

}
