import logging
import os
import time
from core.emane.ieee80211abg import EmaneIeee80211abgModel
from core.emane.nodes import EmaneNet
from core.emulator.coreemu import CoreEmu
from core.emulator.emudata import IpPrefixes, NodeOptions
from core.emulator.enumerations import EventTypes
from core.nodes.base import CoreNode
from core.services.coreservices import CoreService, ServiceMode, ServiceManager

_PATH = os.path.join(os.path.expanduser('~'), ".core")
_SERVICES_PATH = os.path.join(_PATH, "myservices")
EMANE_DELAY = 10


def main():
    ServiceManager.add_services(_SERVICES_PATH)

    # ip generator for example
    prefixes = IpPrefixes(ip4_prefix="10.10.0.0/24")

    # create emulator instance for creating sessions and utility methods
    emu = CoreEmu()
    session = emu.create_session()
    # must be in configuration state for nodes to start, when using "node_add" below
    session.set_state(EventTypes.CONFIGURATION_STATE)

    # create emane network node, emane determines connectivity based on
    # location, so the session and nodes must be configured to provide one
    session.set_location(52.492886, 13.491283, 2.00000, 1.0)
    options_emane = NodeOptions(name="emane_node")
    options_emane.set_position(80, 50)
    emane_network = session.add_node(EmaneNet, options=options_emane, _id=100)
    session.emane.set_model(emane_network, EmaneIeee80211abgModel)

    # create nodes with custom services
    # options_cluster = NodeOptions(name="ClusterNode", model="mdr", services=["Flink_Cluster"])
    # options_generator = NodeOptions(name="GeneratorNode", model="mdr", services=["Flink_Generator"])
    # options_job = NodeOptions(name="JobNode", model="mdr", services=["Flink_Job"])


    options_cluster = NodeOptions(name="ClusterNode", model="mdr")
    options_generator = NodeOptions(name="GeneratorNode", model="mdr")
    options_job = NodeOptions(name="JobNode", model="mdr", services=["FlinkJobService"])

    node_cluster = session.add_node(CoreNode, options=options_cluster)
    node_cluster.setposition(x=150 * 1, y=150)
    interface = prefixes.create_interface(node_cluster)
    session.add_link(node_cluster.id, emane_network.id, interface_one=interface)

    node_generator = session.add_node(CoreNode, options=options_generator)
    node_generator.setposition(x=150 * 2, y=150)
    interface = prefixes.create_interface(node_generator)
    session.add_link(node_generator.id, emane_network.id, interface_one=interface)

    node_job = session.add_node(CoreNode, options=options_job)
    node_job.setposition(x=150 * 3, y=150)
    interface = prefixes.create_interface(node_job)
    session.add_link(node_job.id, emane_network.id, interface_one=interface)

    # session.services.set_service_file(node_cluster.id, "FlinkClusterService", "flink_cluster.sh", "# test")
    # service = session.services.get_service(node_cluster.id, "FlinkClusterService")

    # session.services.set_service_file(node_generator.id, "GeneratorService", "generator.py", "custom file data")


    FLINK_PATH = os.path.join(os.path.expanduser('~'),'Downloads', 'BDAPRO', 'flink-1.10.1')
    cfg_job = "#!/bin/sh\n"
    cfg_job += "# auto-generated by FlinkClusterService (flink_cluster.py)\n"
    cfg_job += os.path.join(FLINK_PATH,'bin/start-cluster.sh')



    session.services.set_service_file(node_job.id, "FlinkJobService", "flink_job.sh", cfg_job)

    # instantiate session
    session.instantiate()

    # OSPF MDR requires some time for routes to be created
    logging.info("waiting %s seconds for OSPF MDR to create routes", EMANE_DELAY)
    time.sleep(EMANE_DELAY)

    # get nodes to run example
    first_node = session.get_node(1, CoreNode)
    last_node = session.get_node(3, CoreNode)
    print(first_node, last_node)
    # address = prefixes.ip4_address(first_node)
    # logging.info("node %s pinging %s", last_node.name, address)
    # output = last_node.cmd(f"ping -c 3 {address}")
    # logging.info(output)

    # shutdown session
    emu.shutdown()


if __name__ == "__main__" or __name__ == "__builtin__":
    logging.basicConfig(level=logging.INFO)
    main()
