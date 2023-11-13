import jade.core.*;
import java.util.ArrayList;
import java.lang.Runtime;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class AgenteInformante extends Agent
{
    private Location origin;
    private int N;
    private long startTime;
    private long endTime;
    private long finalTime;
    private int index;
    
    private long totalFree;
    private ArrayList<String> containers;
    private ArrayList<String> names;
    private ArrayList<Double> cpu;
    private ArrayList<Long> free;

    // Ejecutado por unica vez en la creacin
    
    public void setup()
    {
        origin = here();
        System.out.println("EJECUTO EL SETUP EN: " + origin.getID());

        // Para migrar el agente
        try {
            this.checkArgsAndInitialize();
            startTime = System.currentTimeMillis();
            
            this.migrate();
        } catch (Exception e) {
            System.out.println("No fue posible migrar el agente\n\n\n");}
    }

    // Ejecutado al llegar a un contenedor como resultado de una migracin
    
    protected void afterMove()
    {

        Location actual = here();
        System.out.println("EJECUTO EL AFTERMOVE EN: " + actual.getID());


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
        containers.add(origin.getName());
        
    }

    private void migrate() throws Exception{
        ContainerID destino = new ContainerID(containers.get(index), null);
        System.out.println("MIGRANDO EL AGENTE A " + destino.getID());
        doMove(destino);
    }

    private void print(){
        System.out.println("TIEMPO FINAL: " + finalTime);
        System.out.println("ESPACIO TOTAL DISPONIBLE DE TODAS LAS COMPUTADORAS: " + totalFree);
        for(int i = 0; i < N; i++){
            System.out.println("NOMBRE DE LA COMPUTADORA: " + names.get(i));
            System.out.println("PORCENTAJE DE USO DE LA CPU: " + String.format("%.2f", cpu.get(i)));
        }
        
    }

    
}
