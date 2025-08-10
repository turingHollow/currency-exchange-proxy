package forex.services.caching

import forex.domain.Rate
import forex.services.caching.errors.Error

trait ReadingAlgebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}

trait WritingAlgebra[F[_]] {
  def update(newData: Map[Rate.Pair, Rate]): F[Unit]
}

trait Algebra[F[_]] extends ReadingAlgebra[F] with WritingAlgebra[F]
