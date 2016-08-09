package controllers

import javax.inject._
import akka.actor.{ActorRef, ActorSystem}
import com.rabbitmq.client.{ConnectionFactory, DefaultConsumer}
import com.thenewmotion.akka.rabbitmq.{ChannelActor, ChannelMessage, CreateChannel, _}
import play.api._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Luke on 09/08/2016.
  */
@Singleton
class AkkaController @Inject() extends Controller with App {
  def startPublishSubscribe = Action {
    implicit val system = ActorSystem()

    val HOST_ADDRESS: String = "172.17.25.68"
    val USER_NAME: String = "jesus"
    val USER_PASSWORD: String = "jesus"

    val factory = new ConnectionFactory()
    factory.setHost(HOST_ADDRESS)
    factory.setUsername(USER_NAME)
    factory.setPassword(USER_PASSWORD)
    factory.setConnectionTimeout(60000)
    val connection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
    val exchange = "amq.fanout"

    def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")

    def toBytes(x: Long) = x.toString.getBytes("UTF-8")

    def setupPublisher(channel: Channel, self: ActorRef) {
      val queue = channel.queueDeclare().getQueue
      channel.queueBind(queue, exchange, "")
    }

    def setupSubscriber(channel: Channel, self: ActorRef) {
      val queue = channel.queueDeclare().getQueue
      channel.queueBind(queue, exchange, "")
      val consumer = new DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
          println("received: " + fromBytes(body))
        }
      }
      channel.basicConsume(queue, true, consumer)
    }

    def closeConnection() ={
      system stop connection
    }

    connection ! CreateChannel(ChannelActor.props(setupPublisher), Some("publisher"))
    connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some("subscriber"))

    Future {
      def loop(n: Long) {
        val publisher = system.actorSelection("/user/rabbitmq/publisher")

        def publish(channel: Channel) {
          channel.basicPublish(exchange, "", null, toBytes(n))
        }
        publisher ! ChannelMessage(publish, dropIfNoChannel = false)

        Thread.sleep(1000)
        if (n < 10)
          loop(n + 1)
      }
      loop(0)
      closeConnection()
    }
    Ok(views.html.index("Your new application is ready."))
  }
}
