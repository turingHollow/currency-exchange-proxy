package forex.programs.cacheUpdate

import fs2.Stream

trait Algebra[F[_]] {
  def updatingStream: Stream[F, Unit]
}
