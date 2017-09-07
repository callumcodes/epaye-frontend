/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.epaye.services

import com.codahale.metrics.MetricRegistry
import uk.gov.hmrc.epaye.FrontendAppConfig

trait Partitioner {
  val numberOfPartitions: Int
  def partition(x: Any): Int = math.abs(x.hashCode() % numberOfPartitions)
}

trait PercentagePartitioner extends Partitioner {
  val metricRegistry: MetricRegistry
  def acceptPercentage: Int
  val metricPrefix: String

  override val numberOfPartitions = 100

  def accept(valueToHash: Any): Boolean = {
    val verdict = if (acceptPercentage > 0) partition(valueToHash) <= acceptPercentage else false
    metricRegistry.meter(metricPrefix + verdict).mark()
    verdict
  }
}

trait MetricPartitioner extends Partitioner {
  val metricRegistry: MetricRegistry
  val metricPrefix: String

  override def partition(x: Any): Int = {
    val p = super.partition(x)
    metricRegistry.meter(s"$metricPrefix$p").mark()
    p
  }
}

object MetricPartitioner extends MetricPartitioner {
  override val metricRegistry: MetricRegistry = com.codahale.metrics.SharedMetricRegistries.getOrCreate("default")
  override val metricPrefix: String = "user.partition-"
  override val numberOfPartitions: Int = 10
}

object EPayeLandpPercentagePartitioner extends PercentagePartitioner {
  override val metricRegistry: MetricRegistry = com.codahale.metrics.SharedMetricRegistries.getOrCreate("default")
  override def acceptPercentage: Int = FrontendAppConfig.epayeLandpRampUpPercentage
  override val metricPrefix : String = "epayeLandpShown."
}

object EpayeRelease3PercentagePartitioner extends PercentagePartitioner {
  override val metricRegistry: MetricRegistry = com.codahale.metrics.SharedMetricRegistries.getOrCreate("default")
  override def acceptPercentage: Int = FrontendAppConfig.epayeRelease3RampUpPercentage
  override val metricPrefix : String = "epayeRelease3Shown."
}

object EpayeRelease4PercentagePartitioner extends PercentagePartitioner {
  override val metricRegistry: MetricRegistry = com.codahale.metrics.SharedMetricRegistries.getOrCreate("default")
  override def acceptPercentage: Int = FrontendAppConfig.epayeRelease4RampUpPercentage
  override val metricPrefix : String = "epayeRelease4Shown."
}