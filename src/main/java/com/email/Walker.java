package com.email;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import com.email.hardware.PhysicalSensor;
import com.email.send.SendEmail;

public class Walker extends Thread {

	private int cont = 0; // contador de hilos
	
	public static int TO_PACKAGE = 1; // Tipo de entrega
	public static int SEND = 1; // Estatus enviado 

	public void run() {

		try {
			/* Conexion MySQL */
			Connection connection = DriverManager
					.getConnection("jdbc:mysql://192.168.100.9:3306/db_speedy-packages?" + "autoReconnect=true&useSSL=false&user=raspberry&password=root");
			Statement stmt = connection.createStatement();

			while (true) { // comenzamos el hilo

				System.out.println("INICIALIZANDO EL HILO N° " + cont);
				cont++;

				/* Esperamos un usuario con Tarjeta */
				PhysicalSensor sensor = new PhysicalSensor(); // instanciamos clase
				sensor.on("READ-TAG"); // le pedimos que encienda el lector de tarjetas

				/* Descansamos 3 segundos */
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}

				/* Busca el historial de cards de la maquina */
				ResultSet rs = stmt.executeQuery("SELECT * FROM history");

				/* Obtener consulta */
				while (rs.next()) {

					/* Obtenemos el numero de la tarjeta */
					String cardNumber = rs.getString("card_number");
					System.out.println(cardNumber); // lo imprimimos

					/* Buscamos el usuario de la tarjeta */
					ResultSet rs0 = stmt.executeQuery(
							"SELECT * FROM cards "
							+ "INNER JOIN users "
							+ "ON cards.user_id = users.id "
							+ "WHERE number = '" + cardNumber + "'"
							+ "AND active = " + true + "");
					
					/* Obtener consulta */
					while (rs0.next()) {
						/* Obtenemos datos del usuario */
						int id = rs0.getInt("users.id");
						String name = rs0.getString("users.name");
						String email = rs0.getString("users.email");
						boolean onHold = true;

						/* Busca ordenes en la paqueteria */
						ResultSet rs1 = stmt.executeQuery(
								"SELECT * FROM orders "
								+ "WHERE id_user = " + id
								+ " AND shipping_type = " + TO_PACKAGE
								+ " AND status = " + SEND);
						
						/* Obtener consulta */
						while (rs1.next()) { // obtenemos el primer paquete

							sensor.on("VALID"); // mostramos luz verde
							
							/* Descansamos 3 segundos */
							try {
								TimeUnit.SECONDS.sleep(3);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
							}

							sensor.on("OPEN-BOX"); // abrimos la caja

							/* Descansamos 8 segundos */
							try {
								TimeUnit.SECONDS.sleep(8);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
							}
							
							onHold = false; // Ya no esta a la espera

							try { // Intentamos mandar el correo
								/* Mandamos correo de recibido */
								SendEmail sendEmail = new SendEmail(); // instanciamos correo
								sendEmail.run(name, email, rs1);
							} catch (IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						/* ¿No hay paquetes para el usuario? */
						if (onHold) { // ¿Hay en espera?
							sensor.on("INSUFFICIENT"); // mostramos luz amarilla
							/* Descansamos 3 segundos */
							try {
								TimeUnit.SECONDS.sleep(3);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
							}
						}
						
						/* Borramos el historial de la maquina */
						Statement stmt2 = connection.createStatement();
						stmt2.execute("DELETE FROM history");
					}
				}
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
