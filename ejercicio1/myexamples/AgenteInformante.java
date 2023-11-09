import jade.core.*;
import java.util.ArrayList;
import java.lang.Runtime;

public class AgenteInformante extends Agent
{
    private int N;
    private long startTime;
    private long endTime;
    private long finalTime;
    private int actual;
    
    private long totalFree;
    private ArrayList<String> containers;
    private ArrayList<String> names;
    private ArrayList<Long> times;
    private ArrayList<Long> free;

    // Ejecutado por unica vez en la creacin
    
    public void setup()
    {
        Location origen = here();
        System.out.println("EJECUTO EL SETUP: " + origen.getName());

        // Para migrar el agente
        try {
            this.checkArgsAndInitialize();
            System.out.println("EL VALOR DE N ES: " + N);
            startTime = System.currentTimeMillis();
            
            this.migrate();
        } catch (Exception e) {
            System.out.println("No fue posible migrar el agente\n\n\n");}
    }

    // Ejecutado al llegar a un contenedor como resultado de una migracin
    
    protected void afterMove()
    {

        Location origen = here();
        System.out.println("EJECUTO EL AFTERMOVE: " + origen.getName());
        System.out.println("EL VALOR DE ACTUAL ES: " + actual);

        if(actual != N){
            actual++;
            long start = System.currentTimeMillis();

            free.add(Runtime.getRuntime().freeMemory());

            names.add(origen.getName());

            long end = System.currentTimeMillis();

            times.add(end - start);

            try{
                this.migrate();       
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
            
        } else {
            endTime = System.currentTimeMillis();
            finalTime = endTime - startTime;
            totalFree = 0;
            for(Long f: free)
                totalFree += f;

            this.print();
        }
    }

    private void checkArgsAndInitialize() throws Exception{
        Object[] args = getArguments();

        if(args.length == 0){
            throw new Exception("SE DEBE PASAR UNA LISTA DE CONTAINERS POR ARGS.");
        }

        N = args.length-1;
        containers = new ArrayList<String>();
        names = new ArrayList<String>();
        times = new ArrayList<Long>();
        free = new ArrayList<Long>();
        actual = 0;
        for (int i = 0; i < args.length; i++)
            containers.add((String)args[i]);

        
    }

    private void migrate() throws Exception{
        ContainerID destino = new ContainerID(containers.get(actual), null);
        System.out.println("Migrando el agente a " + destino.getID());
        doMove(destino);
    }

    private void print(){
        System.out.println("TIEMPO FINAL: " + finalTime);
        System.out.println("ESPACIO DISPONIBLE: " + totalFree);
        for(int i = 0; i < N; i++){
            System.out.println("NOMBRE DE LA COMPUTADORA: " + names.get(i));
            System.out.println("TIEMPO DE LA COMPUTADORA: " + times.get(i));
        }
        
    }

    
}
