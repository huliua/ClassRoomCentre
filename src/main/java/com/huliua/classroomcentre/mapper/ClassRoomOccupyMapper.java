package com.huliua.classroomcentre.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huliua.classroomcentre.domain.entity.ClassRoomOccupy;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * @author huliua
 * @version 1.0
 * @date 2025-08-22 17:20
 */
public interface ClassRoomOccupyMapper extends BaseMapper<ClassRoomOccupy> {

    /**
     * 如果教室还有容量就添加教室占用记录
     */
    @Insert("insert into t_classroom_occupy(user_id, classroom_id) select #{userId}, id from t_classroom where id=#{classroomId} and (select count(1) from t_classroom_occupy where classroom_id=#{classroomId})<capacity")
    @Options(useGeneratedKeys = false)
    int addIfHasCapacity(@Param("classroomId") Long classroomId, @Param("userId") String userId);
}
