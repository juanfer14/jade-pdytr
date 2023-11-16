import jade.core.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AgenteMovil extends Agent {

	Location origin;
	String destiny;
	String file;
	int result = 0;

	// Ejecutado por unica vez en la creacion
	public void setup() {

		System.out.println("Creatingn new agent with the follow parameters... ");

		this.origin = here();
		

		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			
			this.destiny = (String)args[0];
			this.file = (String)args[1];
			System.out.println("Creatingn new agent with the follow parameters "+this.destiny +" "+ this.file);
		}
		else {
			// Make the agent terminate immediately
			System.out.println("Parametros no especificados");
			doDelete();
		}

		System.out.println("\n\nHola, agente con nombre local " + getLocalName());
		System.out.println("Se conectara a " + this.destiny + " para leer el archivo " + this.file);
		// Para migrar el agente
		try {
			ContainerID destino = new ContainerID(this.destiny, null);
			System.out.println("Migrando el agente a " + destino.getID() + "\n\n\n");
			doMove(destino);
		} catch (Exception e) {
			System.out.println("\n\n\nNo fue posible migrar el agente\n\n\n");
		}
	}

	// Ejecutado al llegar a un contenedor como resultado de una migracion
	protected void afterMove() {
		if(here().getID().equals(this.origin.getID())) {
			System.out.println("\n\n\nEl resultado es " + this.result + "\n\n\n");
		} else {
			System.out.println("\n\nHola, agente migrado con nombre local " + getLocalName());
			System.out.println("se tratara de acceder al archivo " + this.file);
			try {
				String fileContent = new String(Files.readAllBytes(Paths.get(this.file)));

				fileContent = fileContent.replaceAll("\n", "").replaceAll("\r", "");
            
            	String[] numbers = fileContent.split(",");

				System.out.println("Estos son los valores a sumar");
				for(String n: numbers){
					System.out.println("Numero actual sumando: "+n);
				}

				for(String n : numbers){
					System.out.println("Numero actual sumando: "+n);
					this.result += Integer.parseInt(n);
				}
			} catch (Exception e) {
				this.result = -1;
				e.printStackTrace();
			}
			doMove(this.origin);
		}
	}
}
