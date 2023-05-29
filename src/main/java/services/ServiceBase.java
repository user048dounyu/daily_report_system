package services;

import javax.persistence.EntityManager;

import utils.DBUtil;

public class ServiceBase {
    /**
     * WntityManagerクラス
     */
    protected EntityManager em = DBUtil.createEntityManager();

    /**
     * EntityManagerのクローズ
     */
    public void close() {
        if (em.isOpen()) {
            em.close();
        }
    }



}
