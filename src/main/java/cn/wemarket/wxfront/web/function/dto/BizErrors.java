package cn.wemarket.wxfront.web.function.dto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BizErrors extends BaseDTO{
    private final List<BizError> errors = new LinkedList<BizError>();

    public void reject(String errCode, Object[] errorArgs, String defaultMessage){

    }

    public void addError(BizError error){
        errors.add(error);
    }

    public List<BizError> getAllErrors(){
        return Collections.unmodifiableList(this.errors);
    }

    public boolean hasErrors(){
        return !this.errors.isEmpty();
    }

}
