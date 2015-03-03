package eip;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
public class JdbcEipTest {
    @Test
    public void testInboundChannel() {
        ApplicationContext context
                = new ClassPathXmlApplicationContext("/META-INF/integration/jdbcContext.xml");
//        PersonService service = context.getBean(PersonService.class);
//        logger.info("Creating person Instance");
//        Person person = new Person();
//        Calendar dateOfBirth = Calendar.getInstance();
//        dateOfBirth.set(1980, 0, 1);
//        person.setDateOfBirth(dateOfBirth.getTime());
//        person.setName("Name Of The Person");
//        person.setGender(Gender.MALE);
//        person = service.createPerson(person);
//        Assert.assertNotNull("Expected a non null instance of Person, got null", person);
//        logger.info("\n\tGenerated person with id: " + person.getPersonId() + ", with name: " + person.getName());
    }

}