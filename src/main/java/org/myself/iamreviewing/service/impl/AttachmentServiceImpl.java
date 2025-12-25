package org.myself.iamreviewing.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.NoArgsConstructor;
import org.myself.iamreviewing.domain.dto.AttachmentDTO;
import org.myself.iamreviewing.domain.po.Attachment;
import org.myself.iamreviewing.domain.po.Point;
import org.myself.iamreviewing.domain.vo.AttachmentVO;
import org.myself.iamreviewing.mapper.AttachmentMapper;
import org.myself.iamreviewing.service.PointService;
import org.springframework.stereotype.Service;
import org.myself.iamreviewing.service.AttachmentService;

import java.util.List;

@Service
@NoArgsConstructor
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements AttachmentService {
    @Override
    public List<AttachmentVO> getByPointId(Long id) {

        return convertToVO(list(new LambdaQueryWrapper<Attachment>().eq(Attachment::getPointId, id)));
    }

    @Override
    public boolean addAttachment(Long id, AttachmentDTO attachmentDTO) {
        Attachment attachment = BeanUtil.copyProperties(attachmentDTO, Attachment.class);
        attachment.setPointId(id);
        return save(attachment);
    }

    @Override
    public List<AttachmentVO> getAttachmentsByPointId(Long id) {
        return convertToVO(list(new LambdaQueryWrapper<Attachment>().eq(Attachment::getPointId, id)));
    }

    private  List<AttachmentVO> convertToVO(List<Attachment> list) {
        if (list == null) {return List.of();}
        return BeanUtil.copyToList(list, AttachmentVO.class);
    }
}
