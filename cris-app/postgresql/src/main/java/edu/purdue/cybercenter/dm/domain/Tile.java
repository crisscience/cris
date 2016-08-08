package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "myTilesFilter"),
    @Filter(name = "timeBetweenFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "tile")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Tile extends AbstractCrisAsset {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Tile.getIndex());
    }

    /**
     * *******************************************************
     * Json
     ********************************************************
     * @param ctxPath
     * @return
     */
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(SmallObject.class, fjr);
        useClasses.put(Shortcut.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    public static long countTiles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Tile o", Long.class).getSingleResult();
    }

    public static List<Tile> findAllTiles() {
        return entityManager().createQuery("SELECT o FROM Tile o", Tile.class).getResultList();
    }

    public static Tile findTile(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Tile.class, id);
    }

    public static List<Tile> findTileEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Tile o", Tile.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @ManyToOne
    @JoinColumn(name = "shortcut_id", referencedColumnName = "id")
    private Shortcut shortcutId;

    @Column(name = "html", length = 500)
    @NotNull
    private String html;

    @Column(name = "style", length = 1500)
    private String style;

    @Column(name = "location", length = 10)
    private String location;

    public Shortcut getShortcutId() {
        return shortcutId;
    }

    public void setShortcutId(Shortcut shortcutId) {
        this.shortcutId = shortcutId;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
