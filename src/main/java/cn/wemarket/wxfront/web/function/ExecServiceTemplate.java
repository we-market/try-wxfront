package cn.wemarket.wxfront.web.function;

import cn.wemarket.wxfront.web.function.dto.BaseDTO;
import cn.wemarket.wxfront.web.function.dto.BizErrors;

@FunctionalInterface
public interface ExecServiceTemplate<E extends BaseDTO, T> {
    public T apply(E requestDto, BizErrors bizErrors);

}