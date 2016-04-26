package com.fortysevendeg.ninecards.processes

import java.sql.Timestamp
import java.time.Instant

import com.fortysevendeg.ninecards.processes.NineCardsServices._
import com.fortysevendeg.ninecards.processes.ProcessesExceptions._
import com.fortysevendeg.ninecards.processes.messages.SharedCollectionMessages._
import com.fortysevendeg.ninecards.processes.utils.{ DummyNineCardsConfig, XorMatchers }
import com.fortysevendeg.ninecards.services.free.domain._
import com.fortysevendeg.ninecards.services.persistence._
import doobie.imports._
import org.joda.time.DateTime
import org.specs2.ScalaCheck
import org.specs2.matcher.Matchers
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scalaz.Scalaz._

trait SharedCollectionProcessesSpecification
  extends Specification
  with Matchers
  with Mockito
  with DummyNineCardsConfig
  with SharedCollectionProcessesContext
  with XorMatchers {

  trait BasicScope extends Scope {

    implicit val sharedCollectionPersistenceServices = mock[SharedCollectionPersistenceServices]
    implicit val sharedCollectionSubscriptionPersistenceServices = mock[SharedCollectionSubscriptionPersistenceServices]
    implicit val sharedCollectionProcesses = new SharedCollectionProcesses[NineCardsServices]

    def mockGetSubscription(res: Option[SharedCollectionSubscription]) = {
      sharedCollectionSubscriptionPersistenceServices
        .getSubscriptionByCollectionAndUser(any, any) returns
        res.point[ConnectionIO]
    }

    def mockRemoveSubscription(res: Int) = {
      sharedCollectionSubscriptionPersistenceServices
        .removeSubscriptionByCollectionAndUser(any, any) returns
        res.point[ConnectionIO]
    }

  }

  trait SharedCollectionSuccessfulScope extends BasicScope {

    sharedCollectionPersistenceServices.getCollectionByPublicIdentifier(
      publicIdentifier = publicIdentifier
    ) returns Option(collection).point[ConnectionIO]

    sharedCollectionPersistenceServices.getPackagesByCollection(
      collectionId = collectionId
    ) returns List.empty[SharedCollectionPackage].point[ConnectionIO]

    sharedCollectionPersistenceServices.getCollectionsByUserId(
      userId = publisherId
    ) returns List(collection).point[ConnectionIO]

    sharedCollectionPersistenceServices.getCollectionsByUserId(
      userId = subscriberId
    ) returns List[SharedCollection]().point[ConnectionIO]
  }

  trait SharedCollectionUnsuccessfulScope extends BasicScope {

    sharedCollectionPersistenceServices.getCollectionByPublicIdentifier(
      publicIdentifier = publicIdentifier
    ) returns nonExistentSharedColllection.point[ConnectionIO]
  }

}

trait SharedCollectionProcessesContext {

  val publicIdentifier = "40daf308-fecf-4228-9262-a712d783cf49"

  val collectionId = 1L

  val publisherId = 27L
  val subscriberId = 42L

  val userId = Some(publisherId)

  val now = Timestamp.from(Instant.now)

  val description = Option("Description about the collection")

  val author = "John Doe"

  val name = "The best social media apps"

  val installations = 1

  val views = 1

  val category = "SOCIAL"

  val icon = "path-to-icon"

  val community = true

  val sharedLink = s"http://localhost:8080/collections/$publicIdentifier"

  val collection = SharedCollection(
    id               = collectionId,
    publicIdentifier = publicIdentifier,
    userId           = userId,
    publishedOn      = now,
    description      = description,
    author           = author,
    name             = name,
    installations    = installations,
    views            = views,
    category         = category,
    icon             = icon,
    community        = community
  )

  val nonExistentSharedColllection: Option[SharedCollection] = None

  val sharedCollectionInfo = SharedCollectionInfo(
    publicIdentifier = publicIdentifier,
    publishedOn      = new DateTime(now.getTime),
    description      = description,
    author           = author,
    name             = name,
    sharedLink       = sharedLink,
    installations    = installations,
    views            = views,
    category         = category,
    icon             = icon,
    community        = community,
    packages         = List.empty,
    resolvedPackages = List.empty
  )

  val getCollectionByPublicIdentifierResponse = GetCollectionByPublicIdentifierResponse(
    data = sharedCollectionInfo
  )

  val sharedCollectionNotFoundException = SharedCollectionNotFoundException(
    message = "The required shared collection doesn't exist"
  )

  val subscription = SharedCollectionSubscription(
    id                 = 1L,
    sharedCollectionId = collectionId,
    userId             = subscriberId
  )

}

class SharedCollectionProcessesSpec
  extends SharedCollectionProcessesSpecification
  with ScalaCheck {

  "getCollectionByPublicIdentifier" should {
    "return a valid shared collection info when the shared collection exists" in
      new SharedCollectionSuccessfulScope {
        val collectionInfo = sharedCollectionProcesses.getCollectionByPublicIdentifier(
          publicIdentifier = publicIdentifier
        )

        collectionInfo.foldMap(interpreters).run must beXorRight(getCollectionByPublicIdentifierResponse)
      }

    "return a SharedCollectionNotFoundException when the shared collection doesn't exist" in
      new SharedCollectionUnsuccessfulScope {
        val collectionInfo = sharedCollectionProcesses.getCollectionByPublicIdentifier(
          publicIdentifier = publicIdentifier
        )

        collectionInfo.foldMap(interpreters).run must beXorLeft(sharedCollectionNotFoundException)
      }
  }

  "getPublishedCollections" should {

    "return a list of Shared collections of the publisher user" in new SharedCollectionSuccessfulScope {
      val response = GetPublishedCollectionsResponse(List(sharedCollectionInfo))
      val collectionsInfo = sharedCollectionProcesses.getPublishedCollections(userId = publisherId)
      collectionsInfo.foldMap(interpreters).run mustEqual response
    }

    "return a list of Shared collections of the subscriber user" in new SharedCollectionSuccessfulScope {
      val response = GetPublishedCollectionsResponse(List())
      val collectionsInfo = sharedCollectionProcesses.getPublishedCollections(userId = subscriberId)
      collectionsInfo.foldMap(interpreters).run mustEqual response
    }

  }

  "subscribe" should {

    "return a SharedCollectionNotFoundException when the shared collection does not exist" in new SharedCollectionUnsuccessfulScope {
      val subscriptionInfo = sharedCollectionProcesses.subscribe(publicIdentifier, subscriberId)
      subscriptionInfo.foldMap(interpreters).run must beXorLeft(sharedCollectionNotFoundException)
    }

    "return a valid response if the subscription already exists  " in new SharedCollectionSuccessfulScope {
      mockGetSubscription(Some(subscription))

      val subscriptionInfo = sharedCollectionProcesses.subscribe(publicIdentifier, subscriberId)
      subscriptionInfo.foldMap(interpreters).run must beXorRight(SubscribeResponse())
    }

    "return a valid response if it has created a subscription " in new SharedCollectionSuccessfulScope {
      mockGetSubscription(None)

      sharedCollectionSubscriptionPersistenceServices
        .addSubscription[SharedCollectionSubscription](any, any)(any) returns subscription.point[ConnectionIO]

      val subscriptionInfo = sharedCollectionProcesses.subscribe(publicIdentifier, subscriberId)
      subscriptionInfo.foldMap(interpreters).run must beXorRight(SubscribeResponse())
    }

  }

  "unsubscribe" should {

    "return a SharedCollectionNotFoundException when the shared collection does not exist" in new SharedCollectionUnsuccessfulScope {
      val unsubscription = sharedCollectionProcesses.unsubscribe(publicIdentifier, subscriberId)
      unsubscription.foldMap(interpreters).run must beXorLeft(sharedCollectionNotFoundException)
    }

    "return a valid response if the subscription existed" in new SharedCollectionSuccessfulScope {
      mockRemoveSubscription(1)
      val subscriptionInfo = sharedCollectionProcesses.unsubscribe(publicIdentifier, subscriberId)
      subscriptionInfo.foldMap(interpreters).run must beXorRight(UnsubscribeResponse())
    }

    "return a valid response if the subscription did not existed " in new SharedCollectionSuccessfulScope {
      mockRemoveSubscription(0)

      sharedCollectionSubscriptionPersistenceServices
        .removeSubscriptionByCollectionAndUser(any, any) returns
        0.point[ConnectionIO]

      val subscriptionInfo = sharedCollectionProcesses.unsubscribe(publicIdentifier, subscriberId)
      subscriptionInfo.foldMap(interpreters).run must beXorRight(UnsubscribeResponse())
    }

  }

}
