package io.datonis.examples;

import io.datonis.sdk.EdgeGateway;
import io.datonis.sdk.InstructionHandler;
import io.datonis.sdk.Thing;
import io.datonis.sdk.exception.IllegalThingException;
import io.datonis.sdk.message.AlertType;
import io.datonis.sdk.message.Instruction;
import io.datonis.sdk.GatewayProperties;

import java.util.Random;

import io.datonis.sdk.org.json.simple.JSONArray;
import io.datonis.sdk.org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleAgent
{
    private static final Logger logger = LoggerFactory.getLogger(SampleAgent.class);

    private static final int TRANSMIT_INTERVAL;
    static {
        Number data_transmit_interval = (Number) GatewayProperties.getValue(GatewayProperties.TRANSMIT_INTERVAL_MSEC);
        TRANSMIT_INTERVAL = (data_transmit_interval != null) ? data_transmit_interval.intValue() : 10000;
    }

    private int count = 0;   

    private Thing thing;
    private EdgeGateway gateway;

    private SampleAgent()
    {
        // The EdgeGateway class is the main class for sending data to Datonis. Create an instance and store it for
        // future use.
        this.gateway = new EdgeGateway();
    }

    public static void main(String[] args) throws IllegalThingException
    {
        // First make sure you create an datonis-edge.properties file using your access and secret keys.
        // A sample file should be available with this package
        // The keys are available in the Datonis platform portal

        logger.info("Starting the Sample Datonis Agent");
        // Create and Initialize the agent
        SampleAgent agent = new SampleAgent();

        // Start the Gateway
        if (!agent.startGateway())
        {
            logger.info("Could not start the agent. Exiting");
            return;
        }

        logger.info("Agent started Successfully");

	    // Start a loop to continuously transmit data
	    try {
            while (true) {
                // Transmit data
                logger.info("Transmitting Data");
                agent.transmitData();

                // // Transmit alerts
                // logger.info("Transmitting Alerts");
                // agent.transmitAlerts();

                // Sleep for a specified period before the next iteration
                Thread.sleep(TRANSMIT_INTERVAL); // Sleep for 5 seconds (adjust as needed)
            }
	    } catch (InterruptedException e) {
            logger.error("Agent interrupted: " + e.getMessage(), e);
        } finally {
            logger.info("Exiting");
            agent.stopGateway();
	    }
    }

    private boolean startGateway()
    {
        try
        {
            // Create a Thing Object.It is extremely important use a proper Thing key or collisions may occur and data
            // could get overwritten.
            // Please create a Thing on the Datonis portal and use the key here. Thing can be non-unique, however
            // uniqueness is recommended.
            String thing_key = (String) GatewayProperties.getValue(GatewayProperties.THING_KEY);
            String thing_name = (String) GatewayProperties.getValue(GatewayProperties.THING_NAME);
            String thing_description = (String) GatewayProperties.getValue(GatewayProperties.THING_DESCRIPTION);

            thing = new Thing(thing_key, thing_name, thing_description);

            // You can register multiple things and send data for them. First add the things and then call register.
            // In this case, there is only a single thing object so only a single Thing gets added.
            gateway.addThing(thing);

            // Now start the gateway which automatically registers all the Things. You are now ready to transmit data.
            gateway.start();
        }
        catch (IllegalThingException e)
        {
            logger.error("Could not start the edge gateway: ", e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void setupBiDirectionalCommunication()
    {
        // Register an instruction handler with the gatweay. Whenever Datonis sends an instruction down, the instruction
        // handler is automatically invoked.
        gateway.setInstructionHandler(new InstructionHandler() {

            @Override
            public void handleInstructionExecution(EdgeGateway gateway, Instruction instruction)
            {

                logger.info("Received instruction for thing: " + instruction.getThingKey() + " from Datonis: " + instruction.getInstruction().toJSONString());
                // The instruction.getInstruction() call gives details of the passed instruction. You can now take the
                // appropriate action based on that instruction. `

                JSONObject data = new JSONObject();
                // When the instruction is successfully executed, you should send an execution status of success back to
                // the platform. You can also put additional parameters in there which provides additional context about
                // the instruction executed.
                data.put("execution_status", "success");
                if (!gateway.transmitAlert(instruction.getAlertKey(), instruction.getThingKey(), AlertType.WARNING, "Demo warning, instruction received and logged!", data))
                {
                    logger.error("Could not send Acknowlegement for instruction back to datonis.");
                }
                else
                {
                    logger.info("Sent an instruction acknowlegement back to Datonis!");
                }
            }
        });
    }

    private void transmitData() throws IllegalThingException
    {
        // Construct the JSON packet to be sent. This has to correspond to the metric names. For instance, you have
        // Things that belong to that Thing Template.The metric names that you use here must match the metric names
        // that you have set up in the Thing Template.
        // The format of the data is JSON. We thus build a JSON object.
        JSONObject data = new JSONObject();
        
        String num_tag_1 = (String) GatewayProperties.getValue(GatewayProperties.NUM_TAG_1);
        Number num_tag_value_1 = (Number) GatewayProperties.getValue(GatewayProperties.NUM_TAG_VALUE_1);
        String string_tag_1 = (String) GatewayProperties.getValue(GatewayProperties.STRING_TAG_1);
        String string_tag_value_1 = (String) GatewayProperties.getValue(GatewayProperties.STRING_TAG_VALUE_1);

        data.put(num_tag_1, num_tag_value_1);
        data.put(string_tag_1, string_tag_value_1);

        // //Extra tags for the same thing -> one numeric tag and one string tag

        // String num_tag_2 = (String) GatewayProperties.getValue(GatewayProperties.NUM_TAG_2);
        // Number num_tag_value_2 = (Number) GatewayProperties.getValue(GatewayProperties.NUM_TAG_VALUE_2);
        // String string_tag_2 = (String) GatewayProperties.getValue(GatewayProperties.STRING_TAG_2);
        // String string_tag_value_2 = (String) GatewayProperties.getValue(GatewayProperties.STRING_TAG_VALUE_2);

        // data.put(num_tag_2, num_tag_value_2);
        // data.put(string_tag_2, string_tag_value_2);

        if (!gateway.transmitCompressedData(thing, data, null)) {
            logger.warn("Could not transmit packet: " + count + " value " + data.toJSONString());
        } else {
            count++;
            logger.info("Successfully transmitted data: " + data.toJSONString());
        }
    }

    private void transmitAlert(AlertType alertType, String message)
    {
        // You can send additional information along with the alert in a JSON format.
        JSONObject alertParams = new JSONObject();
        alertParams.put("message", message);

        if (!gateway.transmitAlert(thing.getKey(), alertType, "This is an example " + alertType.toString() + " alert!", alertParams))
        {
            logger.error("Could not send example " + alertType.toString() + " alert");
        }
    }

    public void transmitAlerts()
    {
        // You can transmit 4 kinds of Alerts. INFO, WARNING, ERROR and CRITICAL.You can also transmit parameters along
        // with the alert that will provide additional information about why the alert was generated.
        transmitAlert(AlertType.INFO, "Everything seems okay");
        transmitAlert(AlertType.WARNING, "An issue could occur");
        transmitAlert(AlertType.ERROR, "An issue has happened");
        transmitAlert(AlertType.CRITICAL, "A failure has happened");

    }

    private void stopGateway()
    {
        gateway.stop();
    }

}
