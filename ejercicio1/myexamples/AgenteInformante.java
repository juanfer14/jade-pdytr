import jade.core.*;
import java.util.ArrayList;
import java.lang.Runtime;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class AgenteInformante extends Agent
{
    /* DATOS DE PROCESAMIENTO */

    /*
     * origin: ES LA UBICACION EN DONDE EJECUTA EL SETUP() EL AGENTE.
     * N: INDICA LA CANTIDAD DE CONTAINERS QUE DEBE PROCESAR.
     * startTime: INDICA EL TIEMPO DESDE 
     *            CUANDO SE COMENZO A TOMAR EL TIEMPO TOTAL.
     * endTime: INDICA EL TIEMPO, EN EL QUE SE TERMINO DE PROCESAR
     *          TODOS LOS CONTAINERS.
     * free: GUARDA LA CANTIDAD DE ESPACIO LIBRE DE CADA CONTAINER
     * index: INDICE UTILIZADO PARA IR RECORRIENDO 
     *        Y ALMACENANDO LA INFORMACION DE CADA CONTAINER
     */
    private ArrayList<String> containers;
    private Location origin;
    private int N;
    private long startTime;
    private long finalTime;
    private ArrayList<Long> free;
    private int index;
    
    /* DATOS FINALES */

    /* 
     * endTime: ALMACENA EL TOTAL DE PROCESAMIENTO
     * cpu: GUARDA LA CARGA DE PROCESAMIENTO DE LA CPU 
     *      EN CADA UNO DE LOS CONTAINERS
     * totalFree: GUARDA LA CANTIDAD DE ESPACIO LIRBE 
     *            EN TODOS LOS CONTAINERS.
     * names: GUARDA EL NOMBRE DE CADA UNO DE LOS CONTAINERS.
     */
    private long endTime;
    private ArrayList<Double> cpu;
    private long totalFree;
    private ArrayList<String> names;
    

    // Ejecutado por unica vez en la creacin
    public void setup()
    {
        origin = here();
        System.out.println("EJECUTO EL SETUP EN: " + origin.getID());

        // Para migrar el agente
        try {
            /* VERIFICO QUE SE PASO, ALMENOS UN ARGUMENTO Y INICIALIZA LAS VARIABLES */
            this.checkArgsAndInitialize();

            /* SE EMPIEZA A TOMAR EL TIEMPO, ANTES DE PASARSE AL SIGUIENTE AGENTE */
            startTime = System.currentTimeMillis();
            
            /* SE MIGRA AL SIGUIENTE AGENTE */
            this.migrate();
        } catch (Exception e) {
            System.out.println("No fue posible migrar el agente\n\n\n");}
    }

    // Ejecutado al llegar a un contenedor como resultado de una migracin
    protected void afterMove()
    {

        Location actual = here();
        System.out.println("EJECUTO EL AFTERMOVE EN: " + actual.getID());

        /* SI NO SE LLEGO HASTA EL ULTIMO CONTAINER, SE PROCESA EL ACTUAL */
        if(index != N){

            index++;
            
            free.add(Runtime.getRuntime().freeMemory());

            names.add(actual.getName());

            OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuUsage = operatingSystemMXBean.getSystemCpuLoad();
            cpu.add(cpuUsage * 100);

            try{
                this.migrate();       
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
            
        } else {
            /* 
             * SI SE LLEGO AL ORIGEN, ENTONCES GUARDA EL TIEMPO FINAL Y 
             * SE CALCULA EL ESPACIO DISPONIBLE DE TODOS LOS CONTAINERS  
             */
            endTime = System.currentTimeMillis();
            finalTime = endTime - startTime;
            totalFree = 0;
            for(Long f: free)
                totalFree += f;

            /* SE IMPRIME LA INFORMACION RECOLECTADA. */
            this.print();
        }
    }

    private void checkArgsAndInitialize() throws Exception{
        /* INSTANCIO LOS ARGUMENTOS */
        Object[] args = getArguments();

        /* VERICO SI HAY ARGUMENTOS */
        if(args.length == 0){
            throw new Exception("SE DEBE PASAR UNA LISTA DE CONTAINERS POR ARGS.");
        }

        /* INICIALIZO LAS VARIABLES */
        N = args.length;
        containers = new ArrayList<String>();
        names = new ArrayList<String>();
        cpu = new ArrayList<Double>();
        free = new ArrayList<Long>();
        index = 0;

        System.out.print("LOS CONTAINERS A REVISAR SON: ");
        for (int i = 0; i < N; i++){
            String container = (String)args[i];
            containers.add(container);
            System.out.print(container + " ");
        }
        System.out.println();

        /* 
         * AGREGO EL CONTAINER DEL ORIGEN, A LA LISTA DE CONTAINERS 
         * A PROCESAR, PARA REALIZAR EL ULTIMO MOVIMIENTO A ESTE CONTAINER
         */
        containers.add(origin.getName());
        
    }

    private void migrate() throws Exception{
        /* 
         *  INSTANCIO EL CONTAINER A DONDE DEBO MOVERME, 
         *  SEGUN EL NOMBRE DEL CONTAINER EN EL QUE ESTE ACTUALMENTE EL INDICE. 
         */
        ContainerID destino = new ContainerID(containers.get(index), null);
        System.out.println("MIGRANDO EL AGENTE A " + destino.getID());
        doMove(destino);
    }

    private void print(){
        System.out.println("TIEMPO FINAL EN MS: " + finalTime);
        System.out.println("ESPACIO TOTAL DISPONIBLE EN BYTES, DE TODAS LAS COMPUTADORAS: " + totalFree);
        for(int i = 0; i < N; i++){
            System.out.println("NOMBRE DE LA COMPUTADORA: " + names.get(i));
            System.out.println("PORCENTAJE DE USO DE LA CPU: " + String.format("%.2f", cpu.get(i)));
        }
        
    }

    
}
