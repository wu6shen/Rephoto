#ifndef _FRAME_H_
#define _FRAME_H_

#include "common_include.h"
#include "orb.h"

namespace reloc {
class Frame {
public:
    typedef std::shared_ptr<Frame> Ptr;
    long id_;
    int model_;
    int inlier_num_;
    int inlier_ref_num_;
    double dis2ori_;
    bool is_ori_, use_orb_t_;
    
    cv::Ptr<cv::ORB> orb_;
    ORBextractor *orb_t_;

    
    std::vector<cv::KeyPoint> kpts_kp_;
    std::vector<cv::Point2f> kpts_p2f_;
    std::vector<int> cor_id_ref_;
    std::vector<int> cor_id_ori_;
    cv::Mat desc_;
    
    cv::Mat h_;
    cv::Point2f rect[4];
    
    Frame();
    Frame(long id);
    Frame(long id, cv::Ptr<cv::ORB> orb);

    void setORBT(ORBextractor *t);
    
    void extractORB(cv::Mat &img);
    void updateCorUseKpt(std::vector<cv::DMatch> &matches, Frame &ref);
    cv::Point2f getHPoint(const cv::Point2f &p);
    double getError(const Frame &ori);
    double getDis(const Frame &f);
    void updateInlierNum();
};
}

#endif
