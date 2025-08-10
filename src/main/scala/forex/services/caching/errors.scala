package forex.services.caching

object errors {

  sealed trait Error
  object Error {
    final case class CacheMiss(msg: String) extends Error
  }

}
