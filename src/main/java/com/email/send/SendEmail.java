package com.email.send;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.email.model.Mail;
import com.email.service.EmailService;

@ComponentScan(basePackages = { "com.email.*" })
public class SendEmail {
	
	public void run(String name, String email, ResultSet data) throws IOException, InterruptedException, SQLException {
		
		/* Obtenemos datos */
		
		String orderNumber = data.getString("id");
		String description = data.getString("content");
		float total = data.getFloat("total");
		
		/* Fecha */
		Date date = new Date(); // Sistema actual La fecha y la hora se asignan a date
        System.out.println(date); 
        String strDateFormat = "dd-MMM-yyyy hh:mm:ss"; // El formato de fecha está especificado  
        SimpleDateFormat sort = new SimpleDateFormat(strDateFormat); // La cadena de formato de fecha se pasa como un argumento al objeto 
        System.out.println(sort.format(date)); // El formato de fecha se aplica a la fecha actual
		
		/* Preparamos Correo */
		
		Mail mail = new Mail();
		mail.setMailFrom("your.package.mx@gmail.com"); // correo de la empresa
		mail.setMailTo(email); // correo del usuario
		mail.setMailSubject("Tu Paquete Ha Sido Recibido #" + orderNumber);
		mail.setMailContent("Hola " + name + ",\n\n" +
				"Nos da gusto informarte que el " + sort.format(date) + " recibiste :" + "\n" +
				description + "\n" +
				"Con un monto total de: $" + total + "\n\n" +
				"Si tienes cualquier pregunta, contáctanos.\n" +
				"Atentamente, Speedy Paquetes.");

		ApplicationContext ctx = SpringApplication.run(SendEmail.class);
		EmailService mailService = (EmailService) ctx.getBean("mailService");
		mailService.sendEmail(mail);
	}
}
