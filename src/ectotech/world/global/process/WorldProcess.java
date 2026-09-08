package ectotech.world.global.process;

//Класс для мирового процесса
public abstract class WorldProcess {

    //Подходит для загрузки спрайтов
    public void load(){

    }

    //Здесь могут задаваться стартовые значения/функции, не передаваемые в конструктор такие как -> Events.run(...)
    public void init(){

    }
    //Синхронизированный апдейт (в будущем сделаю систему запросов и синхронизации)
    public void update(){

    }
    //Визуальное обновление (сюда можно впихнуть элементы для отрисовки)
    public void visualUpdate(){

    }

}
