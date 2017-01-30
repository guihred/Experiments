package others;

import java.net.URI;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

public class EWSTest {
	public static void main(String[] args) throws Exception {
		try (ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);) {
			service.setTraceEnabled(true);
			ExchangeCredentials credentials = new WebCredentials("guilherme.fernandes", "11_juuICHI");
			service.setCredentials(credentials);
			service.setUrl(new URI("webmail.ministerio.previdencia"));
			service.setExchange2007CompatibilityMode(true);
			EmailMessage msg = new EmailMessage(service);
			msg.setSubject("Hello world!");
			msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API."));
			msg.getToRecipients().add("someone@contoso.com");
			msg.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
