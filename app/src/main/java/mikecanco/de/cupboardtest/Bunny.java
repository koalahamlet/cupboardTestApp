package mikecanco.de.cupboardtest;

/**
 * Created by koalahamlet on 1/20/15.
 */
public class Bunny {

    public Long _id; // for cupboard
    public String name; // bunny name
    public Integer cuteValue ; // bunny cuteness
    public Integer uglyValue ; // bunny ugly

    public Enum<cutenessType> getCutenessTypeEnum() {
        return cutenessTypeEnum;
    }

    public Enum<cutenessType> cutenessTypeEnum;

    public Bunny() {
        this.name = "steve";
        this.cuteValue = 0;
        this.cutenessTypeEnum = cutenessType.UGLY;
    }
    public Bunny(String name) {
        this.name = name;
        this.cuteValue = (int) (Math.random() * 100);;

        if (cuteValue < 44) {
            cutenessTypeEnum = cutenessType.UGLY;
        }
        else  if (cuteValue < 66) {
            cutenessTypeEnum = cutenessType.CUTE;
        }
        else if (cuteValue < 88) {
            cutenessTypeEnum = cutenessType.VERYCUTE;
        }
        else {
            cutenessTypeEnum = cutenessType.SOCUTEICOULDDIE;
        }

    }

    public static enum cutenessType {
        UGLY, CUTE, VERYCUTE, SOCUTEICOULDDIE;


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCuteValue() {
        return cuteValue;
    }

    public void setCuteValue(Integer cuteValue) {
        this.cuteValue = cuteValue;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

}
