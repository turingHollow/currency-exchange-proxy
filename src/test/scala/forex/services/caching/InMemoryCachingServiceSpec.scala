package forex.services.caching

import cats.effect._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.domain.{Price, Rate, Timestamp}
import TestData._
import forex.config.CachingConfig
import forex.services.caching.errors.Error.{CacheMiss, StaleData}
import forex.services.caching.intepreters.InMemoryCachingService

import scala.concurrent.duration.DurationInt

class InMemoryCachingServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  def createService(ttlMillis: Long): IO[InMemoryCachingService[IO]] = {
    val config = CachingConfig(ttlMillis = ttlMillis, updateDelayMillis = 1, retryDelayMillis = 1)
    InMemoryCachingService.resource[IO](config).use { service =>
      IO.pure(service)
    }
  }

  "InMemoryCachingService" - {
    "when cache is empty" - {
      "should return CacheMiss for empty cache" in {
        for {
          service <- createService(1000)
          result <- service.get(pair1)
        } yield {
          result shouldBe Left(CacheMiss("Data not found"))
        }
      }
    }

    "when cache is populated" - {
      "should return the rate for an existing pair" in {
        for {
          service <- createService(5000)
          _ <- service.update(testData)
          result <- service.get(pair1)
        } yield {
          result shouldBe Right(rate1)
        }
      }

      "should return CacheMiss for a non-existing pair" in {

        for {
          service <- createService(5000)
          _ <- service.update(testData)
          result <- service.get(nonExistingPair)
        } yield {
          result shouldBe Left(CacheMiss("Data not found"))
        }
      }
    }

    "when dealing with TTL" - {
      val shortTtl = 100L

      "should return StaleData if the cache data is older than TTL" in {
        for {
          service <- createService(shortTtl)
          _ <- service.update(testData)
          _ <- IO.sleep(200.millis)
          result <- service.get(pair1)
        } yield {
          result match {
            case Left(StaleData(_, cachedRate, _)) =>
              cachedRate shouldBe rate1
            case other =>
              fail(s"Expected StaleData but got $other")
          }
        }
      }

      "should update the timestamp when cache is updated" in {
        for {
          service <- createService(shortTtl)
          _ <- service.update(testData)
          firstResult <- service.get(pair1)
          _ <- IO.sleep(200.millis)
          _ <- service.update(testData)
          secondResult <- service.get(pair1)
        } yield {
          firstResult shouldBe a[Right[_, _]]
          secondResult shouldBe a[Right[_, _]]
        }
      }
    }

    "when updating the cache" - {
      "should replace existing values" in {
        val updatedRate = Rate(pair1, Price(1.2), Timestamp.now)
        val updatedData = Map(pair1 -> updatedRate)

        for {
          service <- createService(5000)
          _ <- service.update(testData)
          _ <- service.update(updatedData)
          result <- service.get(pair1)
        } yield {
          result shouldBe Right(updatedRate)
        }
      }

      "should remove values not present in the new update" in {
        val partialUpdate = Map(pair1 -> rate1)

        for {
          service <- createService(5000)
          _ <- service.update(testData)
          _ <- service.update(partialUpdate)
          result1 <- service.get(pair1)
          result2 <- service.get(pair2)
        } yield {
          result1 shouldBe Right(rate1)
          result2 shouldBe Left(CacheMiss("Data not found"))
        }
      }
    }
  }
}
