package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type CachingServiceReads[F[_]] = caching.ReadingAlgebra[F]
  type CachingServiceWrites[F[_]] = caching.WritingAlgebra[F]
  final val CachingServices = caching.Interpreters
}
