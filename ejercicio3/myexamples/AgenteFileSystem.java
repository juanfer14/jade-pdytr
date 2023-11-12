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

public class AgenteFileSystem extends Agent {

    private static final int SIZE = 512;

    private boolean isOrigin;
    private boolean finished;

    /* UBICACIONES A DONDE DEBE IR EL AGENTE 
    *       origin: La ubicacion desde donde empieza
    *       destiny: La ubicacion a donde debe leer o escribir     
    */
    private Location origin;
    private Location destiny;
	
    /* ENTRADA */

    /* 
     *      name: Nombre del archivo a leer o escribir
     *      operation: Operacion a realizar 'read' o 'write'
     *      postion: Posicion del archivo a leer o escribir
     *      length: Cantidad de bytes solicitadas para leer o escribir
     */
    private String name;
    private String operation;
    private int position;
    private int length;
    
    /* SALIDA */

    /* DATOS EFECTIVAMENTE LEIDOS O ESCRITOS */
    private int readed;
    private int effective;

    /* DATOS QUE SE RECIBEN DEL READ O DATOS QUE SE DEBEN ESCRIBIR EN EL WRITE  */
    private byte[] data;



    public void setup(){
        try {
            Object[] args = getArguments();

            System.out.println("SE EJECUTO EL SETUP");
            if(args.length > 3 && args.length < 6){
                origin = here();
                isOrigin = true;
                finished = false;
                readed = 0;
                destiny = new ContainerID((String) args[0], null);
                operation = (String) args[1];
                name = (String) args[2];
                length = Integer.parseInt((String)args[3]);

                System.out.println("LA OPERACION ES " + operation);
                System.out.println("EL NOMBRE DEL ARCHIVO ES " + name);
                System.out.println("LA CANTIDAD DE BYTES A LEER ES " + length);
                System.out.println("LA CANTIDAD DE ARGS. SON " + args.length);

                /*  
                *       SI LA OPERACION ES 'READ', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_LEER, POSICION'
                */
                if(operation.equals("read") && length > 0 && args.length == 5){
                    System.out.println("SE EJECUTO EL READ");
                    position = (int) args[4];
                    doMove(destiny);   
                }
                /*  
                *       SI LA OPERACION ES 'WRITE', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_ESCRIBIR'
                */
                else if(operation.equals("write") && length > 0 && args.length == 4){
                    System.out.println("SE EJECUTO EL WRITE");
                    position = 0;
                    read();
                    doMove(destiny);
                }
                else throw new Exception(
                    "LOS ARGS. SON container, operacion (read o write), " + 
                    "nombre_archivo, posicion (solo en read), cant_bytes (mayor a 0)");

            } else throw new Exception("LA CANTIDAD DE ARGS. ES INVALIDA");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("TERMINO EL SETUP");
    }

    protected void afterMove() {
       isOrigin = (origin.getID().equals(here().getID()));

        if(operation.equals("write"))
                if(isOrigin)
                    read();
                else
                    write();
        else if (operation.equals("read")){};

        if(!finished)
            doMove(isOrigin ? destiny : origin);
        else
            System.out.println("TERMINO LA LECTURA/ESCRITURA...");
    }

    private void write(){
        try {
            File file = new File((isOrigin ? "local/" : "fs/") + name);
            FileOutputStream fos = new FileOutputStream(file, true);
            
            fos.write(data, 0, effective);
            fos.close();

            System.out.println("Se escribieron " + effective + " bytes en " + name);


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void read(){
        try {
            /* LEO EL ARCHIVO, DEPENDIENDO EN DONDE ESTE */

            File file = new File((isOrigin ? "local/" : "fs/") + name);
            System.out.println("PATH: " + file.getAbsolutePath());
            
            if(!file.exists())
                throw new IOException("EL ARCHIVO NO EXISTE");

            FileInputStream fis = new FileInputStream(file);
            
            fis.skip(position);

            /* CALCULO CUANTO DEBERIA LEER */
            int nextToRead = Math.min(fis.available(), length);
            nextToRead = Math.min(SIZE, nextToRead);

            if (nextToRead == 0 ||  length == 0)
                finished = true;
            else {
                System.out.println("INSTANCIANDO DATA");
                /* INSTANCIO LOS DATOS A LEER */
                data = new byte[nextToRead];
                
                
                effective = fis.read(data);
                position += effective;
                length -= effective;

                System.out.println("CERRANDO EL ARCHIVO");
                fis.close();

                System.out.println("SE LEYERON " + effective + " BYTES, DESDE " + name);
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}