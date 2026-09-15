package ectotech.world.global.process;

import arc.struct.Seq;

//Статичный класс обновления всего мира. Проще реализовать архитектуру процессов в мире (Взято из Project: Deep sea)
public class WorldProcessHandler {
    //Процессы
    public static Seq<WorldProcess> processes = new Seq<>();

    //Добавление процесса в обработчик
    public static void addProcess(WorldProcess process){
        processes.add(process);
    }
    //Добавление нескольких процессов в обработчик
    public static void addProcesses(WorldProcess ... addProcesses){
        processes.add(addProcesses);
    }

    public static void updateProcesses(){
        for(WorldProcess pr : processes){

        }
    }
}
