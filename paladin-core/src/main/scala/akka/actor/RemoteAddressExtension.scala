package akka.actor

/** [[akka.actor.ActorSystem]] [[Extension]] used to obtain the [[Address]] on which the
 * given ActorSystem is listening.
 *
 * @param system this ActorSystem.
 */
class RemoteAddressExtension(system: ExtendedActorSystem) extends Extension {
  def address: Address = system.provider.getDefaultAddress
}

object RemoteAddressExtension extends ExtensionId[RemoteAddressExtension] {
  override def createExtension(system: ExtendedActorSystem): RemoteAddressExtension = {
    new RemoteAddressExtension(system)
  }
}
