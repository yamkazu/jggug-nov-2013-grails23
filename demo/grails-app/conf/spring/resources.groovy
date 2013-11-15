import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.configuration.ObjectMarshallerRegisterer
import org.codehaus.groovy.grails.web.converters.marshaller.json.InstanceMethodBasedMarshaller
beans = {
    instanceMethodBasedMarshallerRegister(ObjectMarshallerRegisterer) {
        marshaller = new InstanceMethodBasedMarshaller()
        converterClass = JSON
    }
}
