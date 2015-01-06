# Configure the AWS Provider
# debian base ami : ami-1d61a76a
# sim-service ami : ami-24f84b53
provider "aws" {
    access_key = "${var.access_key}"
    secret_key = "${var.secret_key}"
    region = "eu-west-1"
}

resource "aws_security_group" "simservice-sg" {
    name = "simservice-sg"
    description = "Allow all inbound traffic"

    ingress {
        from_port = 0
        to_port = 65535
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }
}

resource "aws_instance" "factory" {
    count = "${var.factory_count}"
    key_name = "sim-service"
    ami = "ami-24f84b53"
    instance_type = "t1.micro"
    tags {
        Name = "SimService node#${count.index}"
        Project = "SimService"
    }
    subnet_id = "${aws_subnet.simservice-net.id}"

    provisioner "local-exec" {
        command = "echo ${element(aws_instance.factory.*.public_ip, count.index)} private_ip=${element(aws_instance.factory.*.private_ip, count.index)}>> private/factories"
    }

}

resource "aws_instance" "core" {
    key_name = "sim-service"
    ami = "ami-24f84b53"
    instance_type = "m1.small"
    tags {
        Name = "SimService core"
        Project = "SimService"
    }
    subnet_id = "${aws_subnet.simservice-net.id}"
}

resource "aws_subnet" "simservice-net" {
  vpc_id = "vpc-6648a303"
  cidr_block = "172.30.3.0/24"
  map_public_ip_on_launch = true
  availability_zone = "eu-west-1b"
  tags {
          Name = "SimService subnet"
          Project = "SimService"
      }
}

# DNS
resource "aws_route53_record" "core-record" {
    zone_id = "Z28O5PDK1WPCSR"
    name = "sim-core.aws.xebiatechevent.info"
    type = "A"
    ttl = "300"
    records = ["${aws_instance.core.public_ip}"]
}

# The following snippet should be available in next terraform release ( current = 0.3.1 )
resource "aws_route53_record" "factory-record" {
    name = "${concat("sim-factory", count.index, ".aws.xebiatechevent.info")}"
    count = "${var.factory_count}"
    zone_id = "Z28O5PDK1WPCSR"
    type = "A"
    records = ["${element(aws_instance.factory.*.public_ip, count.index)}"]
    ttl = "1"
}
