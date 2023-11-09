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
    private int effective;

    /* DATOS QUE SE RECIBEN DEL READ O DATOS QUE SE DEBEN ESCRIBIR EN EL WRITE  */
    private byte[] data;



    public void setup(){
        try {
            Object[] args = getArguments();

            System.out.println("SE EJECUTO EL SETUP");
            if(args.length > 3 && args.length < 6){
                origin = here();
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
                    read(true);
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
       if(operation == "write"){
        
       }
    }

    private void read(boolean local) throws IOException{
        try {
            /* LEO EL ARCHIVO, DEPENDIENDO EN DONDE ESTE */
            System.out.println("INSTANCIANDO FILE");
            File file = new File((local ? "local/" : "fs/") + name);
            System.out.println("PATH: " + file.getAbsolutePath());
            if(!file.exists())
                throw new IOException("EL ARCHIVO NO EXISTE");
            System.out.println("INSTANCIANDO FILEINPUTSTREAM");
            FileInputStream fis = new FileInputStream(file);
            
            /* CALCULO, CUANTO FALTA PARA LEER, DEPENDIENDO DE LO QUE
             SE HAYA PASADO O LA CANTIDAD DISPONIBLE QUE HAYA */
            length = Math.min(length, fis.available());

            System.out.println("INSTANCIANDO DATA");
            /* INSTANCIO LOS DATOS A LEER */
            data = new byte[length];
            
            /* 
            * LEO, HASTA NO TENER MAS BYTES PARA LEER 
            *   -toRead: ES LO QUE FALTA POR LEER
            *   -readed: ES LO QUE SE LEYO ACTUALMENTE
            */
            
            System.out.println("LEYENDO EL ARCHIVO");
            int toRead = length;
            int readed = 0;
            int pos = position;
            while(toRead != 0){
                readed = fis.read(data, pos, toRead);
                pos += readed;
                toRead -= readed;
            }

            System.out.println("CERRANDO EL ARCHIVO");
            fis.close();

            System.out.println("SE LEYERON " + length + " BYTES, DESDE " + name);
        } catch (IOException e){
            throw new IOException("NO SE PUDO LEER EL ARCHIVO...");
        }
    }
}