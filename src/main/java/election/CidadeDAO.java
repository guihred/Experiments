package election;

import java.util.List;
import utils.BaseDAO;

public class CidadeDAO extends BaseDAO {

    public List<Cidade> list() {
        return execute(session -> {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT l ");
            hql.append("FROM Cidade l");
            return session.createQuery(hql.toString(), Cidade.class).list();
        });
	}

}
