import jade.core.*;

import java.lang.IndexOutOfBoundsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class ejer extends Agent {

	Location origin;
	String destiny;
	String fileName;
    String mode;

    FileData file;
    int offset = 0;

	public void setup() {
		try {
            this.origin = here();
            this.mode = (String)getArguments()[0];
            this.fileName = (String)getArguments()[1];

            if(!this.mode.equals("get") && !this.mode.equals("send")) throw new IndexOutOfBoundsException();

            System.out.println("\n\nHola, agente con nombre local " + getLocalName());
            System.out.println("Se movera el archivo " + this.fileName);
            
            // Para migrar el agente
            if(this.mode.equals("get")) {
                doMove(new ContainerID("Main-Container", null));
            } else {
			    afterMove();     
            }
		} catch(IndexOutOfBoundsException e) {
			System.out.println("\n\n\nError en parametros, reinicie el agente enviandole get/send filename\n\n\n");
        } catch (Exception e) {
            e.printStackTrace();
			System.out.println("\n\n\nNo fue posible migrar el agente\n\n\n");
		}
	}

	protected void afterMove() {
        Location next = atHome() ? new ContainerID("Main-Container", null) : this.origin;
		if((atHome() && this.mode.equals("get")) || (!atHome() && this.mode.equals("send"))) {
            write(this.file);
            this.offset += this.file.getAmount();
            if (!this.file.finished()) {
                doMove(next);
            } else {
                System.out.println("\nSe termino de transferir el archivo " + this.fileName + "\n");
            }
		} else {
            if(this.file == null || !this.file.finished()) {
                this.file = read(this.fileName, this.offset, 2048);			
                doMove(next);
            } else {
                System.out.println("\nSe termino de transferir el archivo " + this.fileName + "\n");
            }
		}
	}

    public boolean atHome() {
        return here().getID().equals(this.origin.getID());
    }

    public int write(FileData fileData) {
        try {
            File file = new File("Ej3/" + (this.atHome() ? "local/" : "fs/") + fileData.getName());
            FileOutputStream fos = new FileOutputStream(file,true);
            fos.write(fileData.getData(),0,fileData.getAmount());
            fos.close();
            System.out.println("Se escribieron " + fileData.getAmount() + " bytes en " + fileData.getName());
            return fileData.getAmount();
        } catch (IOException e) {
            System.out.println("Error leyendo archivo " + fileData.getName());
            return -1;
        }
    }

    public FileData read(String fileName, int offset, int amount) {
        try {
            File file = new File("Ej3/" + (this.atHome() ? "local/" : "fs/") + fileName);
            FileInputStream fis = new FileInputStream(file);
            fis.skip(offset);
            byte[] bytesArray = new byte[Math.min(amount,fis.available())]; 
            int amountRead = fis.read(bytesArray);
            boolean finished = fis.available() == 0;
            fis.close();
            System.out.println("Se leyeron " + amountRead + " bytes de " + fileName);
            return new FileData(fileName,bytesArray,finished,amountRead);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error leyendo archivo " + fileName);
            return null;
        }
    }
}

class FileData implements Serializable {

    private String name;
    private byte[] data;
    private boolean finished;
    private int amount;

    public FileData(String name, byte[] data, boolean finished, int amount) {
        this.name = name;
        this.data = data;
        this.finished = finished;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public boolean finished() {
        return finished;
    }

    public int getAmount() {
        return amount;
    }
}